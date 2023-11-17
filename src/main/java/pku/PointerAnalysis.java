package pku;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pascal.taie.World;
import pascal.taie.analysis.ProgramAnalysis;
import pascal.taie.analysis.misc.IRDumper;
import pascal.taie.config.AnalysisConfig;
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

    public PointerAnalysis(AnalysisConfig config) {
        super(config);
    }

    @Override
    public PointerAnalysisResult analyze() {
        var result = new PointerAnalysisResult();
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

        return result;
    }

    private void analyzeMethod(JMethod method) {
        logger.info("{}Analyzing method {}", indent, method);
        indent += " ";
        var stmts = method.getIR().getStmts();
        for (var stmt:stmts)
        {
            logger.info("{}{}: {} ", indent, stmt.getIndex(), stmt.toString());
            if (stmt instanceof Invoke)
            {
                var exp = ((Invoke) stmt).getInvokeExp();
                var methodRef = exp.getMethodRef();
                var invokeMethod = methodRef.resolve();
                analyzeMethod(invokeMethod);
            }
        }
        indent = indent.substring(1);
    }
}
