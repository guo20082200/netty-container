package com.github.netty.core.support;

import com.github.netty.core.util.TodoOptimize;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 回收
 * @author 84215
 */
public abstract class AbstractRecycler<T>   {

    private static final List<AbstractRecycler> RECYCLER_LIST = new LinkedList<>();
    private Stack<T> stack;
    private List<AbstractRecycler> globalList;

    public AbstractRecycler() {
        this(20);
    }

    public AbstractRecycler(int instanceCount) {
        this.stack = new Stack<>();
        RECYCLER_LIST.add(this);
        globalList = RECYCLER_LIST;

        for(int i=0; i< instanceCount; i++) {
            recycle(newInstance());
        }
    }

    public static List<AbstractRecycler> getRecyclerList() {
        return RECYCLER_LIST;
    }

    /**
     * 新建实例
     * @return
     */
    protected abstract T newInstance();

    @TodoOptimize("效果不明显, 先关闭功能")
    public T get() {
        return newInstance();
//        T value = stack.pop();
//        return value != null? value : newInstance();
    }

    @TodoOptimize("效果不明显, 先关闭功能")
    public void recycle(T value) {
//        stack.push(value);
    }

    private class Stack<E> extends ConcurrentLinkedDeque<E> {
//         ReferenceQueue<E> queue = new ReferenceQueue<>();
//
//         public void push0(E value) {
//             new SoftReference<>(value,queue);
//         }
//
//         public E pop0() {
//             Reference<? extends E> reference;
//             try {
//                 reference = queue.remove(1);
//             } catch (InterruptedException e) {
//                 e.printStackTrace();
//                 reference = null;
//             }
//
//             if(reference == null){
//                 return null;
//             }
//
//             E value = reference.get();
//             if(value == null){
//                 return pop();
//             }
//             return value;
//         }

         @Override
         public void push(E e) {
             super.push(e);
         }

         @Override
         public E pop() {
             return super.pollFirst();
         }

     }
}
