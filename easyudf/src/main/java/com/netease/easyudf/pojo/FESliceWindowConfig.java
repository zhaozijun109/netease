package com.netease.easyudf.pojo;

import com.google.common.collect.Lists;
import com.netease.easyml.common.util.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * entity: user/item; attr:blog/item_type/etc...; cross
 * window: before_n
 * target: exposed/click/buy/etc...
 * agg: sum/avg/decay_sum/etc...
 * apply: ctr/cvr/etc...
 */
@Data
public class FESliceWindowConfig {
    public static final String ENTITY = "entity";
    public static final String DAY = "day";

    private static final String COMMA = ",";
    private static final String SEP = "@@";
    private static final String BLANK = "_";
    private static final String TMP = "__tmp__";
    private static final List<String> UDFS = Lists.newArrayList("create or replace temporary cmd create_table as 'com.netease.easyudf.cmd.CreateTable';", "create or replace function cube_concat as 'com.netease.easyudf.udf.util.CubeUDTF';");

    public enum Format {
        WTA, TWA, TAW
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expr {
        private String expr;
        private String as;
        private String target;
        private String window;
        private String agg;
        private String format;

        public Expr(String expr) {
            this.expr = expr;
        }

        public Expr(String expr, String as) {
            this.expr = expr;
            this.as = as;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNoneBlank(expr)) {
                sb.append(expr);
            }
            if (StringUtils.isNoneBlank(as)) {
                if (sb.length() > 0) {
                    sb.append(" as ");
                }
                sb.append(as);
            } else if (StringUtils.isNoneBlank(format)) {
                String name = getAs(format);
                sb.append(" as ").append(name);
            }
            return sb.toString();
        }

        public String getExpr() {
            return StringUtils.isBlank(expr) ? as : expr;
        }

        public String getAs() {
            return StringUtils.isBlank(as) ? expr : as;
        }

        public String getAs(String format) {
            String name;
            if (Format.WTA.name().equals(format)) {
                name = alias(window, target, agg);
            } else if (Format.TAW.name().equals(format)) {
                name = alias(target, agg, window);
            } else {
                name = alias(target, window, agg);
            }
            return name;
        }

        public boolean filterTarget(String target) {
            return filter(this.target, target);
        }

        public boolean filterWindow(String window) {
            return filter(this.window, window);
        }

        public boolean filterAgg(String agg) {
            return filter(this.agg, agg);
        }

        private boolean filter(String a, String b) {
            if (StringUtils.isBlank(a)) {
                return true;
            }
            return Arrays.asList(a.split(COMMA)).contains(b);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exprs {
        private List<Expr> exprs = new ArrayList<>();

        public String toString() {
            return toString(COMMA);
        }

        public String toString(String sep) {
            return exprs.stream().map(Expr::toString).collect(Collectors.joining(sep));
        }

        public void add(Expr expr) {
            exprs.add(expr);
        }
    }

    // multi entities sep by ','
    private List<Expr> entity;
    private List<Expr> target = new ArrayList<>();
    private List<Expr> window = new ArrayList<>();
    private List<Expr> agg = new ArrayList<>();
    private List<Expr> apply = new ArrayList<>();

    private String output;
    private String day;
    private String format = Format.TAW.name();
    private boolean groupBy = true;
    private String cubeFilter;

    private boolean inc;
    private boolean useCube;
    private boolean compact;
    private int partition;

    public List<Expr> getEntity() {
        return entity;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setUseCube(boolean useCube) {
        this.useCube = useCube;
    }

    public void setCubeFilter(String cubeFilter) {
        this.cubeFilter = cubeFilter;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setInc(boolean inc) {
        this.inc = inc;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    private static String alias(String... names) {
        return Arrays.stream(names).filter(StringUtils::isNoneBlank).collect(Collectors.joining(SEP));
    }

    private String render(String template, String fields) {
        return render(template, fields, COMMA);
    }

    private String render(String template, String fields, String sep) {
        if (StringUtils.isNoneBlank(fields)) {
            String[] split = sep != null ? fields.split(sep) : new String[]{fields};
            for (String field : split) {
                int i = template.indexOf("?");
                if (i >= 0) {
                    template = template.substring(0, i) + field + template.substring(i + 1);
                }
            }
        }
        return template;
    }

    private List<String> getEntityKeys() {
        Set<String> uniq = new LinkedHashSet<>();
        for (Expr e : entity) {
            String[] exprs = e.expr.split(COMMA);
            for (String expr : exprs) {
                if (uniq.contains(expr)) {
                    continue;
                }
                uniq.add(expr);
            }
        }
        return new ArrayList<>(uniq);
    }

    private String renderEntityExpr(Exprs targets, String input) {
        Exprs entityExpr = new Exprs();
        for (String key : getEntityKeys()) {
            entityExpr.add(new Expr(key));
        }
        String expr;
        if (inc) {
            List<String> groupBys = new ArrayList<>();
            for (Expr ent : entity) {
                Set<String> prim = Arrays.stream(ent.expr.split(COMMA)).collect(Collectors.toSet());
                Exprs newEnt = new Exprs(entityExpr.exprs.stream().map(it -> prim.contains(it.expr) ? it : new Expr("null", it.getAs())).collect(Collectors.toList()));
                String t = String.format("select %s, %s from %s where %s='%s' group by %s", newEnt, targets, input, ENTITY, ent.getAs(), ent.getExpr());
                groupBys.add(t);
            }
            expr = String.join(" union all ", groupBys);
        } else {
            String groupByExpr = "";
            if (useCube) {
                String condition = entity.stream().map(it -> "(" + Arrays.stream(it.expr.split(COMMA)).map(e -> String.format("tmp.%s is not null", e)).collect(Collectors.joining(" and ")) + ")").collect(Collectors.joining(" or "));
                String whereExpr;
                if (StringUtils.isBlank(cubeFilter)) {
                    whereExpr = "where " + condition;
                } else {
                    whereExpr = String.format("where (%s) and (%s)", condition, cubeFilter);
                }
                if (groupBy) {
                    groupByExpr = String.format("group by %s", entityExpr.exprs.stream().map(e -> "tmp." + e).collect(Collectors.joining(COMMA)));
                }
                String keys = entityExpr.exprs.stream().map(e -> String.format("tmp.%s as %s", e, e)).collect(Collectors.joining(COMMA));
                expr = String.format("select %s, %s from %s lateral view cube_concat(struct(%s))tmp as %s %s %s", keys, targets, input, entityExpr, entityExpr, whereExpr, groupByExpr);
            } else {
                if (groupBy) {
                    groupByExpr = "group by " + entityExpr;
                    if (entity.size() > 1) {
                        groupByExpr += " grouping sets((" + entity.stream().map(it -> it.expr).collect(Collectors.joining("), (")) + "))";
                    }
                }
                expr = String.format("select %s, %s from %s %s", entityExpr, targets, input, groupByExpr);
            }
        }
        return expr;
    }

    private Exprs renderWindowExpr(Exprs targets) {
        if (CollectionUtil.isEmpty(window)) {
            targets.exprs.forEach(it -> it.setTarget(it.getAs()));
            return targets;
        }
        Exprs results = new Exprs();
        for (Expr w : window) {
            for (Expr t : targets.exprs) {
                if (w.filterTarget(t.getAs())) {
                    String expr = "if(" + w.getExpr() + ", " + t.getExpr() + ", null" + ")";
                    results.add(Expr.builder().expr(expr).target(t.getAs()).window(w.as).format(format).build());
                }
            }
        }
        return results;
    }

    private Exprs renderAggExpr(Exprs targets) {
        if (CollectionUtil.isEmpty(agg)) {
            return targets;
        }
        Exprs results = new Exprs();
        for (Expr t : targets.exprs) {
            for (Expr a : agg) {
                if (a.filterWindow(t.window)) {
                    String expr = render(a.getExpr(), t.getExpr(), null);
                    results.add(Expr.builder().expr(expr).target(t.target).window(t.window).agg(a.as).format(format).build());
                }
            }
        }
        return results;
    }

    private Exprs renderApplyExpr() {
        Exprs results = new Exprs();
        for (Expr p : apply) {
            if (StringUtils.isNoneBlank(p.target)) {
                List<String> fields = Arrays.asList(p.target.split(COMMA));
                List<Expr> newAggs = agg.stream().filter(it -> p.filterAgg(it.as)).collect(Collectors.toList());
                List<Expr> newWindows = window.stream().filter(it -> p.filterWindow(it.as)).collect(Collectors.toList());
                if (CollectionUtil.isEmpty(newWindows)) {
                    newWindows = Collections.singletonList(new Expr());
                }
                for (Expr w : newWindows) {
                    for (Expr a : newAggs) {
                        String key = fields.stream().map(it -> Expr.builder().target(it).window(w.as).agg(a.as).build().getAs(format)).collect(Collectors.joining(COMMA));
                        String expr = render(p.expr, key);
                        results.add(Expr.builder().expr(expr).target(p.as).window(w.as).agg(newAggs.size() > 1 ? a.as : "").format(format).build());
                    }
                }
            } else {
                results.add(new Expr(p.expr, p.as));
            }
        }
        return results;
    }

    private List<String> renderOutputExpr(String expr) {
        List<String> exprs = new ArrayList<>();

        List<String> union = new ArrayList<>();
        List<String> primaryKeys = getEntityKeys();
        for (Expr ent : entity) {
            Set<String> keys = Arrays.stream(ent.getExpr().split(COMMA)).collect(Collectors.toSet());
            String cond = primaryKeys.stream().map(it -> it + (keys.contains(it) ? " is not null" : " is null")).collect(Collectors.joining(" and "));
            cond = String.format("when %s then '%s'", cond, ent.as);
            union.add(cond);
        }
        String ent = String.format("case %s else null end as %s", String.join(" ", union), ENTITY);
        expr = String.format("select *, '%s' as day, %s from (%s)t", day, ent, expr);
        expr = String.format("select * from (%s)t where %s is not null", expr, ENTITY);

        if (StringUtils.isNoneBlank(output)) {
            String sql = String.format("CREATE OR REPLACE TEMPORARY VIEW %s as %s;", TMP, expr);
            exprs.add(sql);
            exprs.add(String.format("@create_table(input='%s', output='%s', partitions='day,%s', compact=%s);", TMP, output, ENTITY, compact));
            if (partition > 0) {
                exprs.add(String.format("insert overwrite table %s select /*+ REPARTITION(%d) */ * from %s;", output, partition, TMP));
            } else {
                exprs.add(String.format("insert overwrite table %s select * from %s;", output, TMP));
            }
        } else {
            exprs.add(expr);
        }
        return exprs;
    }

    public String sql(String input) {
        if (!CollectionUtil.isEmpty(agg)) {
            for (Expr a : agg) {
                if (!a.expr.contains("?")) {
                    a.expr += "(?)";
                }
            }
        } else {
            assert entity.size() == 1 && CollectionUtil.isEmpty(apply);
        }
        for (Expr expr : target) {
            if (StringUtils.isBlank(expr.as)) {
                expr.as = expr.expr;
            }
        }
        List<String> exprs = new ArrayList<>(UDFS);
        Exprs targetExpr = new Exprs(target);
        targetExpr = renderWindowExpr(targetExpr);
        targetExpr = renderAggExpr(targetExpr);

        String retExpr = renderEntityExpr(targetExpr, input);
        if (!CollectionUtil.isEmpty(apply)) {
            Exprs applyExpr = renderApplyExpr();
            retExpr = String.format("select *, %s from (%s)t", applyExpr, retExpr);
        }
        exprs.addAll(renderOutputExpr(retExpr));
        exprs = exprs.stream().map(it -> it.replaceAll(SEP, "_")).collect(Collectors.toList());
        return String.join("", exprs);
    }
}

