package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;

public class OOPResultImpl implements OOPResult {

    Throwable e;
    //OOPExpectedException expected;
    OOPTestResult result;

    public OOPResultImpl(Throwable e, OOPExpectedException expected){
        this.e = e;
        //this.expected = expected;
        if (expected != null && expected.getExpectedException() == null){
            if (e == null) {
                result = OOPTestResult.SUCCESS;
                return;
            }
            else if (e instanceof OOPAssertionFailure) {
                result = OOPTestResult.FAILURE;
                return;
            } else {
                result = OOPTestResult.ERROR;
                return;
            }
        }
        else if (expected!=null && expected.getExpectedException()!=null){
            if (e==null){
                result = OOPTestResult.ERROR;
                return;
            }
            if (e instanceof OOPAssertionFailure) {
                result = OOPTestResult.FAILURE;
                return;
            }
            else if (e instanceof Exception) {
                if (expected.assertExpected((Exception)e))
                    result = OOPTestResult.SUCCESS;
                else
                    result = OOPTestResult.EXPECTED_EXCEPTION_MISMATCH;
                return;
            }
        }
        else if (expected == null){
            if (e == null){
                result = OOPTestResult.SUCCESS;
                return;
            }
            if (e instanceof OOPAssertionFailure){
                result = OOPTestResult.FAILURE;
                return;
            }
            else{
                result=OOPTestResult.ERROR;
                return;
            }
        }
    }

    @Override
    public OOPTestResult getResultType(){
       return result;
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
