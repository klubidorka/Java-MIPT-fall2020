import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

public class Main {
    private String field;

    public String getField(){
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public static void recursion(int cnt){
        if (cnt > 0){
            recursion(--cnt);
        } else{
            System.out.println("End");
        }

    }

    public static void main(String[] args) {

    }
}

class ExceptionTest{
    public static void main(String[] args) {
        try{
            int a = 1 + 2;
        } catch (RuntimeException e){
            e.printStackTrace();
        } finally {
            System.out.println("Hello");
        }
    }
}