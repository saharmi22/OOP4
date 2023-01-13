package OOP.Solution;

import OOP.Provided.OOPExpectedException;


import java.util.ArrayList;

public class OOPExpectedExceptionImpl implements OOPExpectedException{
    Class<? extends Exception> expected;
    ArrayList<String> expected_massages;

    private OOPExpectedExceptionImpl(){
        this.expected = null;
        this.expected_massages = new ArrayList<>();
    }

    @Override
    public Class<? extends Exception> getExpectedException() {
        return this.expected;
    }

    @Override
    public OOPExpectedException expect(Class<? extends Exception> expected) {
        this.expected = expected;
        return this;
    }

    @Override
    public OOPExpectedException expectMessage(String msg){
        this.expected_massages.add(msg);
        return this;
    }

    @Override
    public boolean assertExpected(Exception e) {
        if (e!=null) {
            ///
            if (!(this.expected.isAssignableFrom(e.getCause().getClass()))){
                return false;
            }

            String exception_msg = e.getCause().getMessage();
            
            for (String msg : this.expected_massages) {
                if (exception_msg == null && msg != null)
                    return false;
                //assert exception_msg != null;
                if (!exception_msg.contains(msg))
                    return false;
            }
            return true;
        }
        else{
            if (expected!=null)
                return false;
            return true;
        }
    }

    public static OOPExpectedException none(){
        return new OOPExpectedExceptionImpl();
    }
}
