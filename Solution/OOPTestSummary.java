package OOP.Solution;
import OOP.Provided.OOPResult;
import java.util.Map;
import java.util.Objects;


public class OOPTestSummary {
    Map<String, OOPResult> testMap;

    OOPTestSummary (Map<String, OOPResult> testMap){
        this.testMap = testMap;
    }

    int getNumResult(OOPResult.OOPTestResult res_kind){
        int count = 0;
        for (Map.Entry<String,OOPResult> entry : this.testMap.entrySet()){
            if (entry.getValue().getResultType() == res_kind){
                count++;
            }
        }
        return count;
    }

    public int getNumSuccesses(){
        return getNumResult(OOPResult.OOPTestResult.SUCCESS);
    }

    public int getNumFailures() {
        return getNumResult(OOPResult.OOPTestResult.FAILURE);
    }

    public int getNumExceptionMismatches(){
        return getNumResult(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH);
    }

    public int getNumErrors(){
        return getNumResult(OOPResult.OOPTestResult.ERROR);
    }
}
