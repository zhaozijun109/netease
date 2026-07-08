package org.apache.flink.connector.file.sink

import com.google.common.io.ByteStreams
import com.netease.wm.hubble.common.{kerberosConfig => kconf}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.runtime.fs.hdfs.HadoopFileSystem
import org.apache.flink.util.Preconditions
import org.apache.flink.util.concurrent.ExecutorThreadFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.security.UserGroupInformation
import org.slf4j.LoggerFactory
import sun.security.krb5.Config
import java.util.concurrent.TimeUnit
import java.io.{File, FileOutputStream, IOException}
import java.net.URI
import java.security.PrivilegedAction
import java.util.concurrent.{Executors, ScheduledExecutorService}
import java.util.regex.Matcher

case class KerberosConfig(coreSitePath: String, hdfsSitePath: String, krb5ConfPath: String,
                               keyTabPath: String, keyTabLoginUser: String, isRelatedMammunt: Boolean = false)

class SlothHadoopFileSystem(hdfs: org.apache.hadoop.fs.FileSystem) extends HadoopFileSystem(hdfs) {}

object SlothHadoopFileSystem {
  private val LOG = LoggerFactory.getLogger(classOf[SlothHadoopFileSystem])

  private val LOCAL_ENV_SLOTH_FILE_NAME: String = "sloth_test_file_sink"
  private val KERBEROS_CONF: KerberosConfig = KerberosConfig(kconf.CORE_SITE_PATH, kconf.HDFS_SITE_PATH, kconf.KRB5_CONF_PATH, kconf.KEYTAB_PATH, kconf.KEYTAB_LOGIN_USER)
  private val CLASSPATH_SLOTH_FILE: String = "/zz_sloth/"
  private val reloginScheduleTime: Long = 10L
  private val refreshPoolName: String = "sloth-refresh-kerberos"

  val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1, new ExecutorThreadFactory(this.refreshPoolName))

  private var SLOTH_FILE_PATH: String = null

  def getHadoopFileSystem: FileSystem = {
    val hdfs = initFileSystem()
    new SlothHadoopFileSystem(hdfs)
  }

  private def initFileSystem(): org.apache.hadoop.fs.FileSystem = {
    this.LOG.info(" trying to get hadoopEnv, kerberosConfig = " + this.KERBEROS_CONF.toString)
    this.initSlothWorkDir()
    val conf = new Configuration(false)
    this.initHadoopConf(conf)
    val ugi = if (this.KERBEROS_CONF.isRelatedMammunt) {
      this.initMammutEnv(conf)
    } else { this.initKerberosEnv(conf) }

    scheduler.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = {
        try{ reloginExpiringKeytabUser(ugi) }
        catch {
          case var3: Exception =>
            LOG.info("sloth refresh kerberos error:{}", ExceptionUtils.getStackTrace(var3))
        }
      }
    }, 0L, this.reloginScheduleTime, TimeUnit.MINUTES)

    ugi.doAs(new PrivilegedAction[org.apache.hadoop.fs.FileSystem] {
      override def run(): org.apache.hadoop.fs.FileSystem = {
        org.apache.hadoop.fs.FileSystem.get(new URI("hdfs://gy-cluster8/user/virtual_character"), conf)
      }
    })
  }

  @throws[IOException]
  def initHadoopConf(conf: Configuration): Unit = {
    Preconditions.checkNotNull(conf)
    if (this.KERBEROS_CONF.coreSitePath.contains(".xml")) {
      conf.addResource(new Path(this.copyPath(this.KERBEROS_CONF.coreSitePath)))
    } else {
      conf.addResource(new Path(this.copyPath(this.KERBEROS_CONF.coreSitePath, "sloth-mammut-core-site.xml")))
    }

    if (this.KERBEROS_CONF.hdfsSitePath.contains(".xml")) {
      conf.addResource(new Path(this.copyPath(this.KERBEROS_CONF.hdfsSitePath)))
    } else {
      conf.addResource(new Path(this.copyPath(this.KERBEROS_CONF.hdfsSitePath, "sloth-mammut-hdfs-site.xml")))
    }
  }

  @throws[IOException]
  def initKerberosEnv(conf: Configuration): UserGroupInformation = {
    System.setProperty("java.security.krb5.conf",
      if (this.KERBEROS_CONF.krb5ConfPath.contains(".conf")) {
        this.copyPath(this.KERBEROS_CONF.krb5ConfPath)
      } else { this.copyPath(this.KERBEROS_CONF.krb5ConfPath, "sloth-mammut-krb5.conf")}
    )

    try {
      Config.refresh()
      UserGroupInformation.setConfiguration(conf)
      val ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(
        this.KERBEROS_CONF.keyTabLoginUser,
        if (this.KERBEROS_CONF.keyTabPath.contains(".keytab")) {
          this.copyPath(this.KERBEROS_CONF.keyTabPath)
        } else {
          this.copyPath(this.KERBEROS_CONF.keyTabPath, "sloth-mammut.keytab")
        })
      ugi
    } catch {
      case var3: Exception =>
        this.LOG.error("initKerberosEnv error. error:{}", ExceptionUtils.getStackTrace(var3))
        throw new IOException(ExceptionUtils.getStackTrace(var3))
    }
  }

  @throws[IOException]
  private def initMammutEnv(conf: Configuration) = {
    this.LOG.info("start to init mammut env")
    val aa = copyPath("/zz_sloth/sloth-mammut-krb5.conf")
    this.LOG.info("sloth-mammut-krb5.conf  " + aa)
    System.setProperty("java.security.krb5.conf", this.copyPath("/zz_sloth/sloth-mammut-krb5.conf"))
    val in = this.getClass.getResourceAsStream("/zz_sloth/sloth-mammut-principal")
    if (in == null) this.LOG.info("sloth-mammut-principal null")
    val loginUser = IOUtils.toString(in)
    try {
      Config.refresh()
      UserGroupInformation.setConfiguration(conf)
      val ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(loginUser, this.copyPath("/zz_sloth/sloth-mammut.keytab"))
      ugi
    } catch {
      case var6: Exception =>
        this.LOG.error("initKerberosEnv error. error:{}", ExceptionUtils.getStackTrace(var6))
        throw new IOException(ExceptionUtils.getStackTrace(var6))
    }
  }

  @throws[IOException]
  private def copyPath(source: String) = copyAndGetPath(this.getClass, source, this.SLOTH_FILE_PATH, this.getPathFileName(source))

  @throws[IOException]
  private def copyPath(source: String, dist: String) = copyAndGetPath(this.getClass, source, this.SLOTH_FILE_PATH, dist)

  @throws[IOException]
  private def initSlothWorkDir(): Unit = {
    var localDirs = System.getenv.get("LOCAL_DIRS")
    if (localDirs == null) {
      localDirs = System.getProperty("java.io.tmpdir") + File.separator + "sloth_test_file_sink"
      System.out.println("is not cluster env, will init at local path:{}" + localDirs)
      val file = new File(localDirs)
      if (!file.exists) file.mkdir
    }
    if (StringUtils.isBlank(localDirs)) throw new IOException("not find LOCAL_DIRS")
    else {
      val localDir = localDirs.split(",")(0)
      this.SLOTH_FILE_PATH = localDir + File.separator + "slothFile"
      val file = new File(this.SLOTH_FILE_PATH)
      file.mkdirs
      this.LOG.info("initSlothWorkDir success:" + this.SLOTH_FILE_PATH)
    }
  }

  @throws[IOException]
  private def getPathFileName(filePath: String) = if (StringUtils.isBlank(filePath)) throw new IOException("not find file path:" + filePath)
  else {
    val filePaths = filePath.split(Matcher.quoteReplacement(File.separator))
    filePaths(filePaths.length - 1)
  }

  def reloginExpiringKeytabUser(ugi: UserGroupInformation): Unit = {
    try if (ugi.isFromKeytab) {
      ugi.checkTGTAndReloginFromKeytab()
      this.LOG.info("refresh  keytab login success.")
    }
    catch {
      case var3: IOException =>
        this.LOG.info("Error doing relogin using keytab", ExceptionUtils.getStackTrace(var3))
    }
  }

  def copyResourceToPath(clazz: Class[_], resourceName: String, targetPath: String): Unit = {
    try {
      val inputStream = clazz.getResourceAsStream(resourceName)
      var var4: Throwable = null
      try {
        if (inputStream == null) throw new RuntimeException("resource not found.")
        val outputStream = new FileOutputStream(targetPath)
        var var6: Throwable = null
        try ByteStreams.copy(inputStream, outputStream)
        catch {
          case var31: Throwable =>
            var6 = var31
            throw var31
        } finally if (outputStream != null) if (var6 != null) try outputStream.close()
        catch {
          case var30: Throwable =>
            var6.addSuppressed(var30)
        }
        else outputStream.close()
      } catch {
        case var33: Throwable =>
          var4 = var33
          throw var33
      } finally if (inputStream != null) if (var4 != null) try inputStream.close()
      catch {
        case var29: Throwable =>
          var4.addSuppressed(var29)
      }
      else inputStream.close()
    } catch {
      case var35: Exception =>
        throw new RuntimeException("failed to pull resource [" + resourceName + "] out of class to dest [" + targetPath + "]. ex:" + var35.getMessage)
    }
  }

  def copyAndGetPath(clazz: Class[_], source: String, distRootPath: String, distFileName: String): String = {
    val dir = new File(distRootPath)
    if (!dir.exists) dir.mkdirs
    val targetPath = distRootPath + "/" + distFileName
    val file = new File(targetPath)
    if (file.exists) targetPath
    else {
      copyResourceToPath(clazz, source, targetPath)
      targetPath
    }
  }
}
