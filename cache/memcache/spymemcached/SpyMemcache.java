package com.zhangwen.cache.memcache.spymemcached;

import com.zhangwen.cache.memcache.LoggingMemcacheExceptionHandler;
import com.zhangwen.cache.memcache.Memcache;
import com.zhangwen.cache.memcache.MemcacheExceptionHandler;
import com.zhangwen.cache.memcache.utils.StringUtils;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author hy
 * @date 2020/6/5 17:41
 */
public class SpyMemcache implements Memcache {

    private static final Logger log = LoggerFactory.getLogger(SpyMemcache.class);
    private MemcacheExceptionHandler exceptionHandler = new LoggingMemcacheExceptionHandler();

    private final MemcachedClient memcachedClient;

    public SpyMemcache(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public Object get(String key) {
        try {
            log.debug("MemcachedClient.get({})", key);
            return memcachedClient.get(key);
        } catch (Exception e) {
            exceptionHandler.handleErrorOnGet(key, e);
        }
        return null;
    }

    public Map<String, Object> getMulti(String... keys) {
        try {
            return memcachedClient.getBulk(keys);
        } catch (Exception e) {
            exceptionHandler.handleErrorOnGet(StringUtils.join(keys, ", "), e);
        }
        return null;
    }

    public void set(String key, int cacheTimeSeconds, Object o) {
        log.debug("MemcachedClient.set({})", key);
        try {
            memcachedClient.set(key, cacheTimeSeconds, o);
        } catch (Exception e) {
            exceptionHandler.handleErrorOnSet(key, cacheTimeSeconds, o, e);
        }
    }

    public void delete(String key) {
        try {
            memcachedClient.delete(key);
        } catch (Exception e) {
            exceptionHandler.handleErrorOnDelete(key, e);
        }
    }

    public void incr(String key, int factor, int startingValue) {
        try {
            memcachedClient.incr(key, factor, startingValue);
        } catch (Exception e) {
            exceptionHandler.handleErrorOnIncr(key, factor, startingValue, e);
        }
    }

    public void shutdown() {
        log.debug("Shutting down spy MemcachedClient");
        memcachedClient.shutdown();
    }

    public void setExceptionHandler(MemcacheExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
}
