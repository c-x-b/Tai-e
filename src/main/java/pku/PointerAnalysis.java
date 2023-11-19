package pku;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.misc.IRDumper;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.Exp;
import pascal.taie.ir.exp.IntLiteral;
import pascal.taie.ir.exp.InvokeSpecial;
import pascal.taie.ir.exp.InvokeStatic;
import pascal.taie.ir.exp.InvokeVirtual;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.New;
import pascal.taie.language.classes.JMethod;
import pascal.taie.ir.stmt.If;
import pascal.taie.ir.stmt.Copy;
public class PointerAnalysis extends PointerAnalysisTrivial
{
    public static final String ID = "pku-pta";

    private static final Logger logger = LogManager.getLogger(IRDumper.class);

    public static String indent = "";

    public final Map<Exp, List<Integer>> obj_ids;

    public final PointerAnalysisResult result;

    private final File dumpPath = new File("result.txt");

    public PointerAnalysis(AnalysisConfig config) {
        super(config);
        obj_ids = new HashMap<Exp, List<Integer>>();
        result = new PointerAnalysisResult();
        if (dumpPath.exists()) {
            dumpPath.delete();
        }
    }

    @Override
    public PointerAnalysisResult analyze() {
        var preprocess = new PreprocessResult();
        var world = World.get();
        var main = world.getMainMethod();
        var jclass = main.getDeclaringClass();

        // TODO

        // copy
        // storeField & loadField
        // 调用导致的函数内复制传导到函数外
        
        //preprocess.analysis(main.getIR());
        analyzeMethod(main);

        dump(result);

        return result;
    }

    private void analyzeMethod(JMethod method) {
        //logger.info("{}Analyzing method {}", indent, method);
        indent += " ";
        Integer id = 0;
        var stmts = method.getIR().getStmts();
        for (var stmt : stmts) {
            //logger.info("{}{}: {} ", indent, stmt.getIndex(), stmt.toString());
            if (stmt instanceof Invoke) {
                var exp = ((Invoke) stmt).getInvokeExp();
                var methodRef = exp.getMethodRef();
                var invokeMethod = methodRef.resolve();
                var className = methodRef.getDeclaringClass().getName();
                var methodName = methodRef.getName();
                if (className.equals("benchmark.internal.Benchmark")
                        || className.equals("benchmark.internal.BenchmarkN")) {
                    if (methodName.equals("alloc")) {
                        var lit = exp.getArg(0).getConstValue();
                        assert lit instanceof IntLiteral;
                        id = ((IntLiteral) lit).getNumber();
                    } else if (methodName.equals("test")) {
                        var lit = exp.getArg(0).getConstValue();
                        assert lit instanceof IntLiteral;
                        var test_id = ((IntLiteral) lit).getNumber();
                        var pt = exp.getArg(1);
                        var resultIDs = obj_ids.get(pt);
                        var resultTreeSet = new TreeSet<Integer>();
                        if (resultIDs != null)
                            resultTreeSet = new TreeSet<>(resultIDs);
                        result.put(test_id, resultTreeSet);
                        //logger.info("-->testId:{}, result:{}", test_id, resultIDs);
                    }
                } else {
                    analyzeMethod(invokeMethod);
                }
            } else if (stmt instanceof New) {
                var exp = ((New) stmt).getLValue();
                if (id != 0) // ignore unlabeled `new` stmts
                {
                    var ids = obj_ids.get(exp);
                    if (ids == null)
                        ids = new ArrayList<Integer>();
                    ids.add(id);
                    obj_ids.put(exp, ids);
                    //logger.info("-->id:{}, exp:{}", ids, exp);
                }
                id = 0;
            } else if (stmt instanceof Copy) {
                var lval = ((Copy) stmt).getLValue();
                var rval = ((Copy) stmt).getRValue();
                //logger.info("-->LVal:{}, RVal:{}", lval, rval);
                var rids = obj_ids.get(rval);
                if (rids == null)
                    continue;
                var lids = obj_ids.get(lval);
                if (lids == null)
                    lids = new ArrayList<Integer>();
                lids.addAll(rids);
                obj_ids.put(lval, lids);
                //logger.info("-->Lids:{}", lids);
            }
        }
        indent = indent.substring(1);
    }
    
    protected void dump(PointerAnalysisResult result) {
        try (PrintStream out = new PrintStream(new FileOutputStream(dumpPath))) {
            out.println(result);
        } catch (FileNotFoundException e) {
            logger.warn("Failed to dump", e);
        }
    }
}
