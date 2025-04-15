package fitaDNA;

public class Main {
    public static void main(String[] args) {
        Thread t1 = new Thread(new ProcessadorDNA(), "Thread1");
        Thread t2 = new Thread(new ProcessadorDNA(), "Thread2");
        Thread t3 = new Thread(new ProcessadorDNA(), "Thread3");
        Thread t4 = new Thread(new ProcessadorDNA(), "Thread4");
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        
        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("Processamento concluído por todas as threads");
    }
}