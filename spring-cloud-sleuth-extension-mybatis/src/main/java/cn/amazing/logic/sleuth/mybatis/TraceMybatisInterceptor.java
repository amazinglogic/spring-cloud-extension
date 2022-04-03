package cn.amazing.logic.sleuth.mybatis;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxyImpl;
import com.baomidou.mybatisplus.core.MybatisParameterHandler;

import cn.amazing.logic.sleuth.util.ReflectUtils;
import cn.amazing.logic.sleuth.util.SqlUtils;
import cn.amazing.logic.sleuth.util.TraceUtils;

import brave.Span;
import brave.Tracer;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author dengxiaolin
 * @since 2021/01/14
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class TraceMybatisInterceptor implements Interceptor , BeanFactoryAware {


    Tracer tracer;
    Properties properties;
    BeanFactory beanFactory;


    Tracer tracer() {
        if (this.tracer == null) {
            this.tracer = this.beanFactory.getBean(Tracer.class);
        }
        return this.tracer;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Span currentSpan = tracer().currentSpan();
        if (currentSpan == null) {
            return invocation.proceed();
        }
        Span span = tracer().nextSpan();
        span.start();
        // mybatis plus
        MappedStatement mappedStatement = null;
        String name = null;
        try {
            PreparedStatement preparedStatement = getPreparedStatement(invocation);

            span.tag("sql", ((DruidPooledPreparedStatement) preparedStatement).getSql());
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            ParameterHandler parameterHandler = statementHandler.getParameterHandler();

            if (parameterHandler instanceof MybatisParameterHandler) {
                mappedStatement = (MappedStatement) ReflectUtils.getFieldValueByName(parameterHandler, "mappedStatement");
            }
            else {
                PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) ReflectUtils.getFieldValueByName(statementHandler, "delegate");
                mappedStatement = (MappedStatement) ReflectUtils.getFieldValueByName(preparedStatementHandler, "mappedStatement");
            }

            DruidDataSource druidDataSource = (DruidDataSource) mappedStatement.getConfiguration().getEnvironment().getDataSource();
            span.tag("jdbcRef", druidDataSource.getRawJdbcUrl());

            String sqlId = mappedStatement.getId();
            name = TraceUtils.getSimpleName(sqlId);
        }
        catch (Exception e) {
            // ingore
        }
        span.name(name);
        try {
            return invocation.proceed();
        }
        catch (Exception e) {
            span.error(e);
            throw e;
        }
        finally {
            span.finish();
        }
    }

    private static PreparedStatement getPreparedStatement(Invocation invocation) {
        Object arg = invocation.getArgs()[0];
        if (arg instanceof DruidPooledPreparedStatement) {
            PreparedStatementProxyImpl stmtProxy = null;
            try {
                Object stmtObj = ReflectUtils.getFieldValueByName(arg, "stmt");
                if (stmtObj instanceof PreparedStatementProxyImpl) {
                    stmtProxy = (PreparedStatementProxyImpl) stmtObj;
                }
            }
            catch (Exception e) {
                // ignore
            }

            return stmtProxy != null ? stmtProxy.getRawObject() : (DruidPooledPreparedStatement) arg;
        }

        Object stmt = ReflectUtils.getFieldValueByName(ReflectUtils.getFieldValueByName(arg, "h"), "statement");
        if (stmt instanceof PreparedStatementProxyImpl) {
            PreparedStatementProxyImpl stmtProxy = (PreparedStatementProxyImpl) stmt;
            return stmtProxy.getRawObject();
        }
        else {
            return (DruidPooledPreparedStatement) stmt;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
