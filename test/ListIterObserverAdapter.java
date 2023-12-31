
/**
 *
 */
package test;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author Marc Champesme
 * @since 22/08/2023
 * @version 22/08/2023
 *
 */
public class ListIterObserverAdapter<E> extends IterObserverAdapter<E> implements ListIterObserver<E> {
	public static <T> boolean contains(ListIterator<T> listIter, Object o) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.contains​(o);
	}

	public static <T> boolean containsAll(ListIterator<T> listIter, Collection<?> c) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.containsAll(c);
	}

	public static <T> int size(ListIterator<T> listIter) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.size();
	}

	public static <T> boolean isEmpty(ListIterator<T> listIter) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.isEmpty();
	}

	public static <T> T get(ListIterator<T> listIter, int i) {
		ListIterObserver<T> iterObs = new ListIterObserverAdapter<T>(listIter);
		return iterObs.get(i);
	}

	public static <T> boolean isSorted(ListIterator<T> listIter, Comparator<? super T> cmp) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.isSorted(cmp);
	}

	public static <T> List<T> toList(ListIterator<T> listIter) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.toList();
	}

	public static <T> Set<T> toSet(ListIterator<T> listIter) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.toSet();
	}

	public static <T> Object[] toArray(ListIterator<T> listIter) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.toArray();
	}

	public static <T> T[] toArray(ListIterator<T> listIter, T[] a) {
		IterObserver<T> iterObs = new IterObserverAdapter<T>(listIter);
		return iterObs.toArray​(a);
	}

	private ListIterator<E> self;

	public ListIterObserverAdapter() {
		self = this;
	}

	/**
	 *
	 */
	public ListIterObserverAdapter(ListIterator<E> iter) {
		super(iter);
		self = iter;
	}

	private void fastRewind(int count) {
		while (count > 0) {
			self.previous();
			count--;
		}
	}

	private void fastForward(int count) {
		while (count > 0) {
			self.next();
			count--;
		}
	}

	@Override
	public E get(int i) {
		saveLastIndex();
		int count = 0;
		E elt = null;
		boolean noSuchElt = false;
		if (i < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (i >= self.nextIndex()) {
			while (self.hasNext() && i > self.nextIndex()) {
				self.next();
				count++;
			}
			if (self.hasNext()) {
				// i == self.nextIndex()
				elt = self.next();
				self.previous();
			} else {
				noSuchElt = true;
			}
			fastRewind(count);
		} else {
			// i >= 0 && i < nextIndex() && i <= self.previousIndex()
			while (self.hasPrevious() && i < self.previousIndex()) {
				self.previous();
				count++;
			}
			if (self.hasPrevious()) {
				// i == self.previousIndex()
				elt = self.previous();
				self.next();
			} else {
				noSuchElt = true;
			}
			fastForward(count);
		}
		if (noSuchElt) {
			throw new IndexOutOfBoundsException();
		}
		restoreLastIndex();
		return elt;
	}

	@Override
	public boolean hasPrevious() {
		return self.hasPrevious();
	}

	@Override
	public E previous() {
		return self.previous();
	}

	@Override
	public int nextIndex() {
		return self.nextIndex();
	}

	@Override
	public int previousIndex() {
		return self.previousIndex();
	}

	@Override
	public void set(E e) {
		self.set(e);
	}

	@Override
	public void add(E e) {
		self.add(e);
	}
}
