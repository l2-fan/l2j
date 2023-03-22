package l2s.gameserver.core;

import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JavaTest {
    static ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);
    static ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
    static ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

    public static void main(String[] args) {
        Thread thread1 =new Thread(new Runnable() {
            @Override
            public void run() {
                write();
            }
        });

        Thread thread2 =new Thread(new Runnable() {
            @Override
            public void run() {
                read();
                write();

            }
        });
        thread1.start();
        thread2.start();





        thread2.interrupt();
    }
    public static void read(){
        readLock.lock();
        try {
            System.out.println("读"+Thread.currentThread().getId());
        }catch (Exception e ){
            e.printStackTrace();
        }
        finally {
            readLock.unlock();
        }
    }
    public static void write(){
        writeLock.lock();
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("写"+i+"线程"+Thread.currentThread().getId());
            }
        }catch (Exception e ){
            e.printStackTrace();
        }
        finally {
            writeLock.unlock();
        }
    }
}
