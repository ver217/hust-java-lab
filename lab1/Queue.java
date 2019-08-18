import java.util.Stack;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Queue<E> extends Stack<E> {
    public final int dump;
    private Stack<E> stk;

    public Queue() {
        dump = 10;
        stk = new Stack<>();
    }

    public Queue(int size) {
        dump = size;
        stk = new Stack<>();
    }

    public boolean add(E e)
            throws IllegalStateException, ClassCastException, NullPointerException, IllegalArgumentException {
        if (e == null)
            throw new NullPointerException();
        if (stk.size() == dump) {
            if (!super.empty())
                throw new IllegalStateException();
            while (!stk.empty())
                super.push(stk.pop());
        }
        stk.push(e);
        return true;
    }

    public boolean offer(E e) throws ClassCastException, NullPointerException, IllegalArgumentException {
        if (e == null)
            throw new NullPointerException();
        if (stk.size() == dump) {
            if (!super.empty())
                return false;
            while (!stk.empty())
                super.push(stk.pop());
        }
        stk.push(e);
        return true;
    }

    public E remove() throws NoSuchElementException {
        E res = poll();
        if (res == null)
            throw new NoSuchElementException();
        return res;
    }

    public E poll() {
        if (!super.empty()) {
            return super.pop();
        } else if (stk.empty()) {
            return null;
        } else {
            while (stk.size() > 1)
                super.push(stk.pop());
            return stk.pop();
        }
    }

    public E peek() {
        if (!super.empty()) {
            return super.peek();
        } else if (stk.empty()) {
            return null;
        } else {
            while (!stk.empty())
                super.push(stk.pop());
            return super.peek();
        }
    }

    public E element() throws NoSuchElementException {
        E res = peek();
        if (res == null)
            throw new NoSuchElementException();
        return res;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("[");
        Stack<E> tmp = new Stack<>();
        Iterator<E> iter = super.iterator();
        while (iter.hasNext())
            tmp.push(iter.next());
        while (!tmp.empty())
            str.append(tmp.pop() + ",");
        for (E e : stk)
            str.append(e + ",");
        if (str.charAt(str.length() - 1) == ',')
            str.setCharAt(str.length() - 1, ']');
        else
            str.append("]");
        return str.toString();
    }
}
