package com.virjar.dungproxy.client.ippool.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.virjar.dungproxy.client.ippool.PreHeater;
import com.virjar.dungproxy.client.ippool.strategy.AvProxyDumper;
import com.virjar.dungproxy.client.ippool.strategy.Offline;
import com.virjar.dungproxy.client.ippool.strategy.ProxyDomainStrategy;
import com.virjar.dungproxy.client.ippool.strategy.Scoring;
import com.virjar.dungproxy.client.ippool.strategy.impl.BlackListProxyStrategy;
import com.virjar.dungproxy.client.ippool.strategy.impl.DefaultResourceFacade;
import com.virjar.dungproxy.client.ippool.strategy.impl.DefaultScoring;
import com.virjar.dungproxy.client.ippool.strategy.impl.WhiteListProxyStrategy;
import com.virjar.dungproxy.client.model.DefaultProxy;
import com.virjar.dungproxy.client.util.IpAvValidator;

/**
 * client配置 Created by virjar on 16/9/30.
 */
public class Context {
    // IP资源引入器 一般不需要修改
    private String resourceFacade;

    // 代理网站过滤器,通过这个类确认哪些网站需要进行代理
    private ProxyDomainStrategy needProxyStrategy;

    private Offline offliner;

    private int feedBackDuration;

    private AvProxyDumper avProxyDumper;

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    public ProxyDomainStrategy getNeedProxyStrategy() {
        return needProxyStrategy;
    }

    public String getResourceFacade() {
        return resourceFacade;
    }

    private PreHeater preHeater = new PreHeater();

    private List<DefaultProxy> defaultProxyList = Lists.newArrayList();

    private List<String> preHeaterTaskList;

    private int scoreFactory = 10;

    private int minActivityTime = 10 * 60 * 1000;// 一个IP如果超过10分钟没有被使用

    private Scoring scoring = new DefaultScoring();// 暂时硬编码

    private String defaultResourceServerAddress;

    private int preheatSerilizeStep = 20;

    private long globalProxyUseInterval = 0L;

    private Context() {
    }

    public AvProxyDumper getAvProxyDumper() {
        return avProxyDumper;
    }

    // 唯一持有的对象,存储策略
    private static Context instance;
    private static volatile boolean hasInit = false;

    public Scoring getScoring() {
        return scoring;
    }

    public int getScoreFactory() {
        return scoreFactory;
    }

    public long getGlobalProxyUseInterval() {
        return globalProxyUseInterval;
    }

    public Offline getOffliner() {
        return offliner;
    }

    public int getFeedBackDuration() {
        return feedBackDuration;
    }

    public List<DefaultProxy> getDefaultProxyList() {
        return defaultProxyList;
    }

    public List<String> getPreHeaterTaskList() {
        return preHeaterTaskList;
    }

    public String getDefaultResourceServerAddress() {
        return defaultResourceServerAddress;
    }

    public int getPreheatSerilizeStep() {
        return preheatSerilizeStep;
    }

    public static Context getInstance() {
        if (!hasInit) {
            synchronized (Context.class) {
                if (!hasInit) {
                    initEnv(null);
                }
            }
        }
        return instance;
    }

    public static void initEnv(ConfigBuilder builder) {
        if (hasInit) {
            return;
        }
        if (builder == null) {
            builder = ConfigBuilder.create();
            InputStream is = Context.class.getClassLoader().getResourceAsStream(ProxyConstant.configFileName);
            if (is != null) {
                Properties properties = new Properties();
                try {
                    properties.load(is);
                    builder.buildWithProperties(properties);
                } catch (IOException e) {
                    logger.error("error when load config file", e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }

        }
        instance = builder.build();
        hasInit = true;
    }

    public static class ConfigBuilder {
        private String resouceFace;

        private String proxyDomainStrategy;
        private String proxyDomainStrategyBlackList;
        private String proxyDomainStrategyWhiteList;

        private String offliner;

        private String feedBackDuration;

        private String avDumper;
        private String defaultAvDumpeFileName;
        private String defaultProxyList;
        private String preHeaterTaskList;

        private String defaultResourceServerAddress;
        private String preheaterSerilizeStep;

        private String proxyUseInterval;

        public ConfigBuilder buildWithProperties(Properties properties) {
            if (properties == null) {
                return this;
            }
            resouceFace = properties.getProperty(ProxyConstant.RESOURCE_FACADE, ProxyConstant.DEFAULT_RESOURCE_FACADE);
            proxyDomainStrategy = properties.getProperty(ProxyConstant.PROXY_DOMAIN_STRATEGY,
                    ProxyConstant.DEFAULT_DOMAIN_STRATEGY);
            proxyDomainStrategyBlackList = properties.getProperty(ProxyConstant.BLACK_LIST_STRATEGY);
            proxyDomainStrategyWhiteList = properties.getProperty(ProxyConstant.WHITE_LIST_STRATEGY);

            feedBackDuration = properties.getProperty(ProxyConstant.FEEDBACK_DURATION);

            avDumper = properties.getProperty(ProxyConstant.PROXY_SERIALIZER);
            defaultAvDumpeFileName = properties.getProperty(ProxyConstant.DEFAULT_PROXY_SERALIZER_FILE);
            defaultProxyList = properties.getProperty(ProxyConstant.DEFAULT_PROXY_LIST);
            preHeaterTaskList = properties.getProperty(ProxyConstant.PREHEATER_TASK_LIST);
            defaultResourceServerAddress = properties.getProperty(ProxyConstant.DEFAULT_RESOURCE_SERVER_ADDRESS);
            preheaterSerilizeStep = properties.getProperty(ProxyConstant.PREHEAT_SERIALIZE_STEP);
            proxyUseInterval = properties.getProperty(ProxyConstant.PROXY_USE_INTERVAL);
            return this;
        }

        public static ConfigBuilder create() {
            return new ConfigBuilder();
        }

        public ConfigBuilder setResouceFace(String resouceFace) {
            this.resouceFace = resouceFace;
            return this;
        }

        public ConfigBuilder setAvDumper(String avDumper) {
            this.avDumper = avDumper;
            return this;
        }

        public ConfigBuilder setDefaultAvDumpeFileName(String defaultAvDumpeFileName) {
            this.defaultAvDumpeFileName = defaultAvDumpeFileName;
            return this;
        }

        public ConfigBuilder setFeedBackDuration(String feedBackDuration) {
            this.feedBackDuration = feedBackDuration;
            return this;
        }

        public ConfigBuilder setOffliner(String offliner) {
            this.offliner = offliner;
            return this;
        }

        public ConfigBuilder setProxyDomainStrategy(String proxyDomainStrategy) {
            this.proxyDomainStrategy = proxyDomainStrategy;
            return this;
        }

        /**
         * 设置默认的代理,如果当前代理池还拿不到代理,则尝试使用默认,默认代理应该为一个转发服务器
         * 
         * @param defaultProxyList ip:port
         * @return configBuilder
         */
        public ConfigBuilder setDefaultProxyList(String defaultProxyList) {
            this.defaultProxyList = defaultProxyList;
            return this;
        }

        public ConfigBuilder setProxyDomainStrategyBlackList(String proxyDomainStrategyBlackList) {
            this.proxyDomainStrategyBlackList = proxyDomainStrategyBlackList;
            return this;
        }

        public ConfigBuilder setProxyDomainStrategyWhiteList(String proxyDomainStrategyWhiteList) {
            this.proxyDomainStrategyWhiteList = proxyDomainStrategyWhiteList;
            return this;
        }

        private Context build() {
            Context context = new Context();
            // 服务器地址
            if (StringUtils.isEmpty(defaultResourceServerAddress)) {
                defaultResourceServerAddress = ProxyConstant.SERVER_ADDRESS;
            }
            context.defaultResourceServerAddress = defaultResourceServerAddress;

            // resouceFace
            context.resourceFacade = StringUtils.isEmpty(this.resouceFace) ? ProxyConstant.DEFAULT_RESOURCE_FACADE
                    : this.resouceFace;

            resolveDefaultResourceFacade(context);

            // domainStrategy
            if (StringUtils.isEmpty(proxyDomainStrategy)) {
                proxyDomainStrategy = ProxyConstant.DEFAULT_DOMAIN_STRATEGY;
            }
            switch (proxyDomainStrategy) {
            case "WHITE_LIST":
                WhiteListProxyStrategy whiteListProxyStrategy = new WhiteListProxyStrategy();
                if (!StringUtils.isEmpty(proxyDomainStrategyWhiteList)) {
                    for (String domain : Splitter.on(",").omitEmptyStrings().trimResults()
                            .split(proxyDomainStrategyWhiteList)) {
                        whiteListProxyStrategy.addWhiteHost(domain);
                    }
                }
                context.needProxyStrategy = whiteListProxyStrategy;
                break;
            case "BLACK_LIST":
                BlackListProxyStrategy blackListProxyStrategy = new BlackListProxyStrategy();
                if (!StringUtils.isEmpty(proxyDomainStrategyBlackList)) {
                    for (String domain : Splitter.on(",").omitEmptyStrings().trimResults()
                            .split(proxyDomainStrategyBlackList)) {
                        blackListProxyStrategy.add2BlackList(domain);
                    }
                }
                context.needProxyStrategy = blackListProxyStrategy;
                break;
            default:
                context.needProxyStrategy = ObjectFactory.newInstance(proxyDomainStrategy);
            }

            // offliner
            if (this.offliner == null) {
                offliner = "com.virjar.dungproxy.client.ippool.strategy.impl.DefaultOffliner";
            }
            context.offliner = ObjectFactory.newInstance(offliner);

            if (this.feedBackDuration == null) {
                feedBackDuration = "120000";
            }
            context.feedBackDuration = NumberUtils.toInt(feedBackDuration, 120000);

            // avDumper
            if (this.avDumper == null) {
                this.avDumper = ProxyConstant.DEFAULT_PROXY_SERIALIZER;
            }
            if (defaultAvDumpeFileName == null) {
                defaultAvDumpeFileName = ProxyConstant.DEFAULT_PROXY_SERALIZER_FILE_VALUE;
            }
            context.avProxyDumper = ObjectFactory.newInstance(avDumper);
            context.avProxyDumper.setDumpFileName(defaultAvDumpeFileName);

            // default proxy
            resolveDefaultProxy(this.defaultProxyList, context);

            if (StringUtils.isNotEmpty(this.preHeaterTaskList)) {
                context.preHeaterTaskList = Splitter.on(",").omitEmptyStrings().trimResults()
                        .splitToList(preHeaterTaskList);
            } else {
                context.preHeaterTaskList = Lists.newArrayList();
            }

            // 预热增量序列化时机
            context.preheatSerilizeStep = NumberUtils.toInt(preheaterSerilizeStep,
                    ProxyConstant.DEFAULT_PREHEATER_SERILIZE_STEP);

            // 全局的IP最小使用间隔
            context.globalProxyUseInterval = NumberUtils.toInt(proxyUseInterval, 0);
            if (context.globalProxyUseInterval < 0) {
                context.globalProxyUseInterval = 0;
            }
            return context;
        }

        /**
         * 如果是服务器默认资源导入器,特殊处理,因为他在class层面提供扩展,同时也在配置上面提供扩展
         * 
         * @param context
         */
        void resolveDefaultResourceFacade(Context context) {
            if (ProxyConstant.DEFAULT_RESOURCE_FACADE.equals(context.resourceFacade)) {
                DefaultResourceFacade.setAllAvUrl(context.defaultResourceServerAddress + "/proxyipcenter/allAv");
                DefaultResourceFacade.setAvUrl(context.defaultResourceServerAddress + "/proxyipcenter/av");
                DefaultResourceFacade.setFeedBackUrl(context.defaultResourceServerAddress + "/proxyipcenter/feedBack");
            }
        }

        void resolveDefaultProxy(String proxyString, Context context) {
            if (StringUtils.isEmpty(proxyString)) {
                return;
            }
            try {
                Map<String, String> map = Splitter.on(",").omitEmptyStrings().trimResults().withKeyValueSeparator(":")
                        .split(proxyString);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    int port = NumberUtils.toInt(entry.getValue(), 8081);
                    if (IpAvValidator.validateProxyConnect(new HttpHost(entry.getKey(), port))) {
                        DefaultProxy defaultProxy = new DefaultProxy();
                        defaultProxy.setIp(entry.getKey());
                        defaultProxy.setPort(port);
                        context.defaultProxyList.add(defaultProxy);
                    }
                }
                logger.info("统一代理服务,有效地址:{}", JSONObject.toJSONString(context.defaultProxyList));
            } catch (Exception e) {
                logger.warn("默认代理加载失败,不能识别的格式:{}", proxyString);
            }
        }
    }

    public PreHeater getPreHeater() {
        return preHeater;
    }
}
