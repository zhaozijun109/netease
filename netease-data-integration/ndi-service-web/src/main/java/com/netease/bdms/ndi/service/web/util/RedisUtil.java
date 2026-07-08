package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.exception.RedisException;
import com.netease.bdms.ndi.service.web.service.ProjectConfigService;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Component
public class RedisUtil {

  private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

  private static RedisUtil redisUtil;

  @Autowired
  private ProjectConfigService configService;

  private JedisPool jedisPool;

  @PostConstruct
  private void initPool() {
    String redisIp = configService.getConfig(CommonConstants.RedisConfig.IP);
    Integer redisPort = Integer.parseInt(configService.getConfig(CommonConstants.RedisConfig.PORT));
    String password = configService.getConfig(CommonConstants.RedisConfig.PASSWORD);
    Integer maxTotal = Integer.parseInt(configService.getConfig(CommonConstants.RedisConfig.MAX_TOTAL));
    Integer maxIdle = Integer.parseInt(configService.getConfig(CommonConstants.RedisConfig.MAX_IDLE));
    Integer minIdle = Integer.parseInt(configService.getConfig(CommonConstants.RedisConfig.MIN_IDLE));
    JedisPoolConfig config = new JedisPoolConfig();
    config.setMaxActive(maxTotal);
    config.setMaxIdle(maxIdle);
    config.setMinIdle(minIdle);
    jedisPool = new JedisPool(config, redisIp, redisPort, 1000 * 5, password);
    if (jedisPool == null) {
      throw new RedisException("Failed to connect redis.");
    }
    redisUtil = this;
  }

  public String keyBuilder(String... keys) {
    StringBuilder stringBuilder = new StringBuilder();
    for (String key : keys) {
      stringBuilder.append("_").append(key);
    }
    return stringBuilder.toString();
  }

  private Jedis getJedis() {
    if (jedisPool != null) {
      return jedisPool.getResource();
    }
    return null;
  }

  private void close(Jedis jedis) {
    if (jedis != null) {
      jedisPool.returnResource(jedis);
    }
  }

  public String set(String key, String value) {
    Jedis jedis = null;
    String result = null;
    try {
      jedis = getJedis();
      result = jedis.set(key, value);
    } catch (Exception e) {
      log.error("set key{},value{},error:", key, value, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public Long hset(String key, String field, String value) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.hset(key, field, value);
    } catch (Exception e) {
      log.error("set key{},value{},error:", key, value, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  /**
   * 带有超时时间的hset
   *
   * @param key
   * @param field
   * @param value
   * @param exTime 超时时间，s
   * @return
   */
  public Long hsetWithExpire(String key, String field, String value, int exTime) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.hset(key, field, value);
      expire(key, exTime);
    } catch (Exception e) {
      log.error("set key{},value{},error:", key, value, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public Long hsetnx(String key, String field, String value) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.hsetnx(key, field, value);
    } catch (Exception e) {
      log.error("set key{},value{},error:", key, value, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public String hget(String key, String field) {
    Jedis jedis = null;
    String result = null;
    try {
      jedis = getJedis();
      result = jedis.hget(key, field);
    } catch (Exception e) {
      log.error("get key:{}, field:{}. error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  /**
   * If the field was present in the hash it is deleted and 1 is returned, otherwise 0 is
   * returned and no operation is performed.
   *
   * @param key
   * @param fields
   * @return
   */
  public Long hdel(final String key, final String... fields) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.hdel(key, fields);
    } catch (Exception e) {
      log.error("get key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  /**
   * Return all the fields in a hash.
   * <p>
   * <b>Time complexity:</b> O(N), where N is the total number of entries
   *
   * @param key
   * @return All the fields names contained into a hash.
   */
  public Set<String> hkeys(final String key) {
    Jedis jedis = null;
    Set<String> result = null;
    try {
      jedis = getJedis();
      result = jedis.hkeys(key);
    } catch (Exception e) {
      log.error("get key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public List<String> hvals(final String key) {
    Jedis jedis = null;
    List<String> result = null;
    try {
      jedis = getJedis();
      result = jedis.hvals(key);

    } catch (Exception e) {
      log.error("get key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public Boolean hexists(final String key, final String field) {
    Jedis jedis = null;
    Boolean result = null;
    try {
      jedis = getJedis();
      result = jedis.hexists(key, field);
    } catch (Exception e) {
      log.error("get key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;

  }

  /**
   * @param key
   * @param value
   * @return 1 if the key was set, 0 if the key was not set
   */
  public Long setnx(String key, String value) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.setnx(key, value);
    } catch (Exception e) {
      log.error("set key{},value{},error:", key, value, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public String setEx(String key, String value, int exTime) {
    Jedis jedis = null;
    String result = null;
    try {
      jedis = getJedis();
      result = jedis.setex(key, exTime, value);
    } catch (Exception e) {
      log.error("setex key{},value{},error:", key, value, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public String get(String key) {
    Jedis jedis = null;
    String result = null;
    try {
      jedis = getJedis();
      result = jedis.get(key);
    } catch (Exception e) {
      log.error("get key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public Long del(String key) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.del(key);
    } catch (Exception e) {
      log.error("del key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public Long expire(String key, int exTime) {
    Jedis jedis = null;
    Long result = null;
    try {
      jedis = getJedis();
      result = jedis.expire(key, exTime);
    } catch (Exception e) {
      log.error("del key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public Boolean exists(final String key) {
    Jedis jedis = null;
    Boolean result = null;
    try {
      jedis = getJedis();
      result = jedis.exists(key);
    } catch (Exception e) {
      log.error("get key{},error:", key, e);
      close(jedis);
      return result;
    }
    close(jedis);
    return result;
  }

  public static RedisUtil getRedisUtil() {
    return redisUtil;
  }

}
