package org.elasticsearch.dsl.helper;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.dsl.enums.SortOption;
import org.elasticsearch.dsl.exception.ElasticSql2DslException;

import java.util.List;

public class ElasticSqlMethodInvokeHelper {
    public static final List<String> DATE_METHOD = ImmutableList.of("date", "to_date", "toDate");
    public static final List<String> NVL_METHOD = ImmutableList.of("nvl", "is_null", "isnull");

    public static final List<String> AGG_TERMS_METHOD = ImmutableList.of("terms", "terms_agg");
    public static final List<String> AGG_RANGE_METHOD = ImmutableList.of("range", "range_agg");
    public static final List<String> AGG_RANGE_SEGMENT_METHOD = ImmutableList.of("segment", "segment_agg");

    public static final String AGG_MIN_METHOD = "min";
    public static final String AGG_MAX_METHOD = "max";
    public static final String AGG_AVG_METHOD = "avg";
    public static final String AGG_SUM_METHOD = "sum";

    public static Boolean isMethodOf(List<String> methodAlias, String method) {
        if (CollectionUtils.isEmpty(methodAlias)) {
            return Boolean.FALSE;
        }
        for (String alias : methodAlias) {
            if (alias.equalsIgnoreCase(method)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public static Boolean isMethodOf(String methodAlias, String method) {
        if (StringUtils.isBlank(methodAlias)) {
            return Boolean.FALSE;
        }
        return methodAlias.equalsIgnoreCase(method);
    }

    public static void checkTermsAggMethod(SQLMethodInvokeExpr aggInvokeExpr) {
        if (!isMethodOf(AGG_TERMS_METHOD, aggInvokeExpr.getMethodName())) {
            throw new ElasticSql2DslException("[syntax error] Sql not support method:" + aggInvokeExpr.getMethodName());
        }
    }

    public static void checkRangeAggMethod(SQLMethodInvokeExpr aggInvokeExpr) {
        if (!isMethodOf(AGG_RANGE_METHOD, aggInvokeExpr.getMethodName())) {
            throw new ElasticSql2DslException("[syntax error] Sql not support method:" + aggInvokeExpr.getMethodName());
        }
    }

    public static void checkRangeItemAggMethod(SQLMethodInvokeExpr aggInvokeExpr) {
        if (!isMethodOf(AGG_RANGE_SEGMENT_METHOD, aggInvokeExpr.getMethodName())) {
            throw new ElasticSql2DslException("[syntax error] Sql not support method:" + aggInvokeExpr.getMethodName());
        }
    }

    public static void checkStatAggMethod(SQLAggregateExpr statAggExpr) {
        if (!AGG_MIN_METHOD.equalsIgnoreCase(statAggExpr.getMethodName()) &&
                !AGG_MAX_METHOD.equalsIgnoreCase(statAggExpr.getMethodName()) &&
                !AGG_AVG_METHOD.equalsIgnoreCase(statAggExpr.getMethodName()) &&
                !AGG_SUM_METHOD.equalsIgnoreCase(statAggExpr.getMethodName())) {
            throw new ElasticSql2DslException("[syntax error] Sql not support method:" + statAggExpr.getMethodName());
        }
    }

    public static void checkDateMethod(SQLMethodInvokeExpr dateInvokeExpr) {
        if (!isMethodOf(DATE_METHOD, dateInvokeExpr.getMethodName())) {
            throw new ElasticSql2DslException("[syntax error] Sql not support method:" + dateInvokeExpr.getMethodName());
        }

        if (CollectionUtils.isEmpty(dateInvokeExpr.getParameters()) || dateInvokeExpr.getParameters().size() != 2) {
            throw new ElasticSql2DslException(String.format("[syntax error] There is no %s args method named date",
                    dateInvokeExpr.getParameters() != null ? dateInvokeExpr.getParameters().size() : 0));
        }

        SQLExpr patternArg = dateInvokeExpr.getParameters().get(0);
        SQLExpr timeValArg = dateInvokeExpr.getParameters().get(1);

        if (!(patternArg instanceof SQLCharExpr) && !(patternArg instanceof SQLVariantRefExpr)) {
            throw new ElasticSql2DslException("[syntax error] The first arg of date method should be a time pattern");
        }

        if (!(timeValArg instanceof SQLCharExpr) && !(timeValArg instanceof SQLVariantRefExpr)) {
            throw new ElasticSql2DslException("[syntax error] The second arg of date method should be a string of time");
        }
    }

    public static void checkNvlMethod(SQLMethodInvokeExpr nvlInvokeExpr) {
        if (!isMethodOf(NVL_METHOD, nvlInvokeExpr.getMethodName())) {
            throw new ElasticSql2DslException("[syntax error] Sql sort condition only support nvl method invoke");
        }

        if (CollectionUtils.isEmpty(nvlInvokeExpr.getParameters()) || nvlInvokeExpr.getParameters().size() > 3) {
            throw new ElasticSql2DslException(String.format("[syntax error] There is no %s args method named nvl",
                    nvlInvokeExpr.getParameters() != null ? nvlInvokeExpr.getParameters().size() : 0));
        }

        SQLExpr fieldArg = nvlInvokeExpr.getParameters().get(0);
        SQLExpr valueArg = nvlInvokeExpr.getParameters().get(1);

        if (!(fieldArg instanceof SQLPropertyExpr) && !(fieldArg instanceof SQLIdentifierExpr)) {
            throw new ElasticSql2DslException("[syntax error] The first arg of nvl method should be field param name");
        }

        if (!(valueArg instanceof SQLCharExpr) && !(valueArg instanceof SQLIntegerExpr) && !(valueArg instanceof SQLNumberExpr)) {
            throw new ElasticSql2DslException("[syntax error] The second arg of nvl method should be number or string");
        }

        if (nvlInvokeExpr.getParameters().size() == 3) {
            SQLExpr sortModArg = nvlInvokeExpr.getParameters().get(2);
            if (!(sortModArg instanceof SQLCharExpr)) {
                throw new ElasticSql2DslException("[syntax error] The third arg of nvl method should be string");
            }
            String sortModeText = ((SQLCharExpr) sortModArg).getText();
            if (!SortOption.AVG.mode().equalsIgnoreCase(sortModeText) && !SortOption.MIN.mode().equalsIgnoreCase(sortModeText)
                    && !SortOption.MAX.mode().equalsIgnoreCase(sortModeText) && !SortOption.SUM.mode().equalsIgnoreCase(sortModeText)) {
                throw new ElasticSql2DslException("[syntax error] The third arg of nvl method should be one of the string[min,max,avg,sum]");
            }
        }
    }
}
