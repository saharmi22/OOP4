package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

public class OOPResultImpl implements OOPResult {

    Throwable e;
    OOPExpectedException expected;

    public OOPResultImpl(Throwable e, OOPExpectedException expected){
        this.e = e;
        this.expected = expected;
    }

    @Override
    public OOPTestResult getResultType(){
        if (expected == null){
            if (e == null)
                return  OOPTestResult.SUCCESS;
            else if (e instanceof OOPAssertionFailure) {
                    return OOPTestResult.FAILURE;
            } else
                return OOPTestResult.ERROR;
        }
        else {
            if (e instanceof OOPAssertionFailure)
                return OOPTestResult.FAILURE;
            else if (e instanceof Exception) {
                if (expected.assertExpected((Exception)e))
                    return OOPTestResult.SUCCESS;
            }
            return OOPTestResult.EXPECTED_EXCEPTION_MISMATCH;
        }
    }

    @Override
    public String getMessage() {
        if (e != null) {
            if (this.getResultType() == OOPTestResult.ERROR){
                return e.getClass().getName();
            }
            return this.e.getMessage();
        }
        return null;
    }

    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (obj instanceof OOPResult){
            String msg1 = this.getMessage();
            String msg2 = ((OOPResult)obj).getMessage();
            OOPTestResult result1 = this.getResultType();
            OOPTestResult result2 = ((OOPResult)obj).getResultType();
            return result2 == result1 && msg1.equals(msg2);
        }
        return false;
    }
}
