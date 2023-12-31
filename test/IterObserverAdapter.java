package test;

/**
 *
 */

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import social.model.ExtendedListIterator;
import social.model.FusionSortedIterator;

/**
 *
 */
public class IterObserverAdapter<E> implements IterObserver<E> {
	public static void main(String[] args) {
		List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 2, 3, 5, 8, 9);
		List<Integer> list1 = List.of(2, 3, 7, 9, 9, 3);
		List<Integer> list2 = List.of(2, 3, 7, 9, 9, 100);
		List<Integer> list3 = List.of(512, 423, 123, 10, 9, 5, 1);
		System.out.println("Ma liste: " + list);
		System.out.println("list1: " + list1);
		ListIterator<Integer> listIter = list.listIterator(5);
		ListIterObserver<Integer> iterObs = new ListIterObserverAdapter<Integer>(listIter);
		System.out.println("iter.nextIndex()=" + listIter.nextIndex());
		System.out.println("list.size()=" + list.size());
		System.out.println("iter.size()=" + iterObs.size());
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("iter.contains(1)=" + iterObs.contains​(1));
		System.out.println("iter.contains(9)=" + iterObs.contains​(9));
		System.out.println("iter.contains(5)=" + iterObs.contains​(5));
		System.out.println("iter.contains(55)=" + iterObs.contains​(55));
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("iter.containsAll(list1)=" + iterObs.containsAll(list1));
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("iter.get(3)=" + iterObs.get(3));
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("iter.get(0)=" + iterObs.get(0));
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("iter.asList()=" + iterObs.toList());
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("iter.asSet()=" + iterObs.toSet());
		System.out.println("iter.nextIndex()=" + iterObs.nextIndex());
		System.out.println("list triée: " + list2);
		ListIterObserver<Integer> iterObs2 = new ListIterObserverAdapter<Integer>(list2.listIterator());
		System.out.println("iter2.isSorted(naturalOrder)=" + iterObs2.isSorted(Comparator.naturalOrder()));
		System.out.println("list triée: " + list3);
		ListIterObserver<Integer> iterObs3 = new ListIterObserverAdapter<Integer>(list3.listIterator());
		System.out.println("iter3.isSorted(reverseOrder)=" + iterObs3.isSorted(Comparator.reverseOrder()));
	}

	private ListIterator<E> self;
	private int oldLastIndex;

	/**
	 * Constructeur réservé à l'usage exclusif des sous-classes implémentant
	 * l'interface ListIterator.
	 *
	 * @throws ClassCastException si utilisé pour créer une instance de cette classe
	 *                            ou d'une sous-classe n'implémentant pas
	 *                            l'interface ListIterator
	 */
	@SuppressWarnings("unchecked")
	public IterObserverAdapter() {
		self = (ListIterator<E>) this;
	}

	public IterObserverAdapter(ListIterator<E> listIter) {
		self = listIter;
	}

	@Override
	public boolean hasNext() {
		return self.hasNext();
	}

	@Override
	public E next() {
		return self.next();
	}

	@Override
	public void remove() {
		self.remove();
	}

	public void saveLastIndex() {
		if (self instanceof ExtendedListIterator<?>) {
			ExtendedListIterator<?> iter = (ExtendedListIterator<?>) self;
			this.oldLastIndex = iter.lastIndex();

		}
		if (self instanceof FusionSortedIterator<?, ?>) {
			FusionSortedIterator<?, ?> iter = (FusionSortedIterator<?, ?>) self;
			this.oldLastIndex = iter.lastIndex();
		}
	}

	public void restoreLastIndex() {
		int newLastIndex = -2;
		if (self instanceof ExtendedListIterator<?>) {
			ExtendedListIterator<?> iter = (ExtendedListIterator<?>) self;
			newLastIndex = iter.lastIndex();
			if (newLastIndex == oldLastIndex) {
				return;
			}
			if (oldLastIndex == -1) {
				iter.startIteration();
				return;
			}
		}
		if (self instanceof FusionSortedIterator<?, ?>) {
			FusionSortedIterator<?, ?> iter = (FusionSortedIterator<?, ?>) self;
			newLastIndex = iter.lastIndex();
			if (newLastIndex == oldLastIndex) {
				return;
			}
			if (oldLastIndex == -1) {
				iter.startIteration();
				return;
			}
		}
		if (newLastIndex == -2) {
			return;
		}
		// newLastIndex != oldLastIndex
		// ==> oldLastIndex != -1 && newLastIndex != -1
		// ==> oldLastIndex != size - 1 && newLastIndex != size - 1
		if (newLastIndex == self.previousIndex()) {
			self.next();
			self.previous();
		} else {
			self.previous();
			self.next();
		}
	}

	/**
	 * @pure
	 */
	@Override
	public boolean contains​(Object o) {
		saveLastIndex();
		int count = 0;
		boolean found = false;
		// Recherche entre la position courante et la fin
		while (self.hasNext() && !found) {
			if (Objects.equals(o, self.next())) {
				found = true;
			}
			count++;
		}
		// Retour à la position initiale
		fastRewind(count);
		if (found) {
			restoreLastIndex();
			return true;
		}
		count = 0;
		// Recherche entre le début et la position initiale
		while (self.hasPrevious() && !found) {
			if (Objects.equals(o, self.previous())) {
				found = true;
			}
			count++;
		}
		// Retour à la position initiale
		fastForward(count);
		restoreLastIndex();
		return found;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		saveLastIndex();
		Set<Object> s = new HashSet<Object>(c);
		int count = 0;
		while (self.hasNext() && !s.isEmpty()) {
			s.remove(self.next());
			count++;
		}
		fastRewind(count);
		if (s.isEmpty()) {
			restoreLastIndex();
			return true;
		}
		count = 0;
		while (self.hasPrevious() && !s.isEmpty()) {
			s.remove(self.previous());
			count++;
		}
		fastForward(count);
		restoreLastIndex();
		return s.isEmpty();
	}

	@Override
	public int size() {
		saveLastIndex();
		int size = self.nextIndex();
		int count = fastForward();

		size = size + count;
		fastRewind(count);
		restoreLastIndex();
		return size;
	}

	@Override
	public boolean isEmpty() {
		return !self.hasNext() && !self.hasPrevious();
	}

	@Override
	public Object[] toArray() {
		return toList().toArray();
	}

	@Override
	public <T> T[] toArray​(T[] a) {
		return toList().toArray(a);
	}

	@Override
	public List<E> toList() {
		saveLastIndex();
		List<E> result = new LinkedList<E>();
		int countPrevious = fastRewind();
		int countNext = 0;

		while (self.hasNext()) {
			result.add(self.next());
			if (countPrevious > 0) {
				countPrevious--;
			} else {
				countNext++;
			}
		}

		fastRewind(countNext);
		restoreLastIndex();
		return result;
	}

	@Override
	public Set<E> toSet() {
		saveLastIndex();
		Set<E> result = new HashSet<E>();
		int countPrevious = fastRewind();
		int countNext = 0;

		while (self.hasNext()) {
			result.add(self.next());
			if (countPrevious > 0) {
				countPrevious--;
			} else {
				countNext++;
			}
		}
		fastRewind(countNext);
		restoreLastIndex();
		return result;
	}

	@Override
	public boolean isSorted(Comparator<? super E> cmp) {
		saveLastIndex();
		E prev = null;
		E current = null;
		int countNext = 0;
		int countPrevious = fastRewind();
		// !hasPrevious()
		if (self.hasNext()) {
			prev = self.next();
			if (countPrevious > 0) {
				countPrevious--;
			} else {
				countNext++;
			}
		} else {
			// isEmpty() ==> isSorted
			restoreLastIndex();
			return true;
		}
		while (self.hasNext()) {
			current = self.next();
			if (countPrevious > 0) {
				countPrevious--;
			} else {
				countNext++;
			}
			if (cmp.compare(prev, current) > 0) {
				// prev > current
				fastRewind(countNext);
				restoreLastIndex();
				return false;
			}
			prev = current;
		}
		fastRewind(countNext);
		restoreLastIndex();
		return true;
	}

	private void fastRewind(int count) {
		while (count > 0) {
			self.previous();
			count--;
		}
	}

	private int fastRewind() {
		int count = 0;
		while (self.hasPrevious()) {
			self.previous();
			count++;
		}
		return count;
	}

	private void fastForward(int count) {
		while (count > 0) {
			self.next();
			count--;
		}
	}

	private int fastForward() {
		int count = 0;
		while (self.hasNext()) {
			self.next();
			count++;
		}
		return count;
	}

}
