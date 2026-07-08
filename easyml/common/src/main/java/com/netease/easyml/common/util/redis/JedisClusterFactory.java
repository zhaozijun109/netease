package com.netease.easyml.common.util.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class JedisClusterFactory {

    private Integer timeout;
    private Integer maxRedirections;
    private String hosts;
    private String password;
    private GenericObjectPoolConfig genericObjectPoolConfig;

    private Pattern p = Pattern.compile("^.+[:]\\d{1,5}\\s*$");

    public Integer getTimeout() {
        return timeout;
    }

    public JedisClusterFactory setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public Integer getMaxRedirections() {
        return maxRedirections;
    }

    public JedisClusterFactory setMaxRedirections(Integer maxRedirections) {
        this.maxRedirections = maxRedirections;
        return this;
    }

    public String getHosts() {
        return hosts;
    }

    public JedisClusterFactory setHosts(String hosts) {
        this.hosts = hosts;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JedisClusterFactory setPassword(String password) {
        this.password = password;
        return this;
    }

    public GenericObjectPoolConfig getGenericObjectPoolConfig() {
        return genericObjectPoolConfig;
    }

    public JedisClusterFactory setGenericObjectPoolConfig(GenericObjectPoolConfig genericObjectPoolConfig) {
        this.genericObjectPoolConfig = genericObjectPoolConfig;
        return this;
    }

    public Pattern getP() {
        return p;
    }

    public JedisClusterFactory setP(Pattern p) {
        this.p = p;
        return this;
    }

    public JedisCluster getJedisCluster() throws Exception {
        Set<HostAndPort> haps = parseHostAndPort();
        return new JedisCluster(haps, timeout, timeout, maxRedirections, genericObjectPoolConfig);
    }

    private Set<HostAndPort> parseHostAndPort() throws Exception {
        try {
            Set<HostAndPort> haps = new HashSet<>();
            String[] hostAttr = hosts.split(",");
            for (String val : hostAttr) {

                boolean isIpPort = p.matcher(val).matches();

                if (!isIpPort) {
                    throw new IllegalArgumentException("illegal ip or port");
                }
                String[] ipAndPort = val.split(":");

                HostAndPort hap = new HostAndPort(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
                haps.add(hap);
            }

            return haps;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new Exception("failed to parse jedis config", ex);
        }
    }


    public static GenericObjectPoolConfig newPoolConfig(long maxWaitMillis, int maxTotal, int maxIdle) {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxWaitMillis(maxWaitMillis);
        genericObjectPoolConfig.setMaxTotal(maxTotal);
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        return genericObjectPoolConfig;
    }
}
