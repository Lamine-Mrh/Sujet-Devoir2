package social.model.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import social.model.FusionSortedIterator;
import social.model.ExtendedListIterator;
import social.model.Post;
import social.model.User;

/**
 * Test class for FusionSortedIterator.
 *
 * Un ListIterator de Post fusionnant plusieurs Listerator de Post en
 * interdisant toutes les opérations de modification (i.e. add, remove, set).
 *
 * Un FusionSortedIterator garantie que, si les ListIterator fusionnés sont
 * ordonnés par date (du plus récent au plus ancien), alors ce
 * DateSortedIterator sera également ordonné par date (du plus récent au plus
 * ancien).
 */
public class TestFusionSortedIterator {
	public static Stream<FusionSortedIterator<Post, User>> fusSortedIterProvider() {
		return Stream.generate(DataProvider::fusSortedIterSupplier).limit(DataProvider.LG_STREAM);
	}

	public static Stream<Set<User>> setOfListIterProvider() {
		Set<User> iterList = DataProvider.setOfListIterSupplier();
//		if (!iterList.isEmpty()) {
//			iterList.set(Suppliers.randInt(iterList.size()), null);
//		}
		iterList.add(null);
		Stream<Set<User>> exStream = Stream.of(null, iterList);
		return Stream.concat(exStream, Stream.generate(DataProvider::setOfListIterSupplier))
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> iterAndPostProvider() {
		return Stream.generate(() -> Arguments.of(DataProvider.fusSortedIterSupplier(), DataProvider.postSupplier()))
				.limit(DataProvider.LG_STREAM);
	}

	private int prevIndex;
	private int nextIndex;
	private int lastIndex;
	private User lastUser;
	private List<Post> content;
	private ListIterObserver<Post> iterModel;
	private Comparator<? super Post> comparator;

	private void setModel(FusionSortedIterator<Post, User> self) {
		iterModel = new ListIterObserverAdapter<Post>(self);
	}

	private void saveState(FusionSortedIterator<Post, User> self) {
		content = iterModel.toList();
		prevIndex = self.previousIndex();
		nextIndex = self.nextIndex();
		lastIndex = self.lastIndex();
		lastUser = self.lastIterator();
		comparator = self.comparator();
	}

	private void assertPurity(FusionSortedIterator<Post, User> self) {
		assertEquals(content, iterModel.toList());
		assertEquals(nextIndex, self.nextIndex());
		assertEquals(prevIndex, self.previousIndex());
		assertEquals(lastIndex, self.lastIndex());
		assertEquals(lastUser, self.lastIterator());
		assertEquals(comparator, self.comparator());
	}

	public void assertInvariant(FusionSortedIterator<Post, User> self) {
		// @invariant !iterModel.contains(null);
		assertFalse(iterModel.contains​(null));
		// @invariant iterModel.isSorted(comparator());
		assertTrue(iterModel.isSorted(self.comparator()));
		// @invariant nextIndex() == previousIndex() + 1;
		assertEquals(self.nextIndex(), self.previousIndex() + 1);
		// @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex();
		assertTrue(self.lastIndex() == self.nextIndex() || self.lastIndex() == self.previousIndex());
		// @invariant previousIndex() >= -1;
		assertTrue(self.previousIndex() >= -1 && self.previousIndex() < iterModel.size());
		// @invariant nextIndex() >= 0;
		assertTrue(self.nextIndex() >= 0 && self.nextIndex() <= iterModel.size());
		// @invariant lastIndex() >= -1;
		assertTrue(self.lastIndex() >= -1 && self.lastIndex() < iterModel.size());
		// @invariant lastIndex() == -1 <==> lastIterator() == null;
		assertEquals(self.lastIndex() == -1, self.lastIterator() == null);
		// @invariant !hasPrevious() <==> previousIndex() == -1;
		assertEquals(!self.hasPrevious(), self.previousIndex() == -1);
		// @invariant comparator() != null;
		assertNotNull(self.comparator());
	}

	/**
	 * Test method for constructor FusionSortedIterator
	 *
	 * Initialise une instance permettant d'itérer sur tous les Post des
	 * ListIterator de la liste spécifiée. Il s'agit donc d'une fusion de tous les
	 * ListIterator contenus dans la liste spécifiée.
	 *
	 * @throws NullPointerException si la liste spécifiée est null ou contient null
	 */
	@ParameterizedTest
	@MethodSource("setOfListIterProvider")
	public void testFusionSortedIterator(Set<User> postIterators) {

		// Pré-conditions:
		// @requires postIterators != null && !postIterators.contains(null);
		if (postIterators == null || postIterators.contains(null)) {
			assertThrows(NullPointerException.class,
					() -> new FusionSortedIterator<Post, User>(postIterators, Comparator.reverseOrder()));
			return;
		}
		// @requires (\forall ListIterator<Post> iter; postIterators.contains(iter);
		// !contains(iter, null));
		// @requires (\forall ListIterator<Post> iter; postIterators.contains(iter);
		// isSorted(iter, Comparator.reverseOrder()));

		// Oldies:

		// Exécution:
		FusionSortedIterator<Post, User> result = new FusionSortedIterator<Post, User>(postIterators,
				Comparator.reverseOrder());

		// Post-conditions:
		setModel(result);


		// @ensures (\forall ListIterator<Post> iter; postIterators.contains(iter);
		// iterModel.containsAll(toList(iter)));
		int totalSize = 0;
		for (User iter : postIterators) {
			List<Post> l = ListIterObserverAdapter.toList(iter);
			// @ensures lastIndex() == -1;
			assertTrue(result.lastIndex() == -1);
			assertTrue(iterModel.containsAll(l));
			// @ensures lastIndex() == -1;
			assertTrue(result.previousIndex() == -1);
			assertTrue(result.nextIndex() == 0);
			assertTrue(result.lastIndex() == -1);
			totalSize += ListIterObserverAdapter.size(iter);
			// @ensures lastIndex() == -1;
			assertTrue(result.lastIndex() == -1);
		}
		// @ensures lastIndex() == -1;
		assertTrue(result.lastIndex() == -1);

		// @ensures iterModel.size() == (\sum ListIterator<Post> iter;
		// postIterators.contains(iter); size(iter));
		assertEquals(totalSize, iterModel.size());
		// @ensures (\forall Post p; iterModel.contains(p); (\exists ListIterator<Post>
		// iter; postIterators.contains(iter); contains(iter, p)));
		Iterator<Post> iter = iterModel.toList().iterator();
		while (iter.hasNext()) {
			Post p = iter.next();
			boolean found = false;
			Iterator<User> liter = postIterators.iterator();
			while (liter.hasNext() && !found) {
				User listIter = liter.next();
				found = ListIterObserverAdapter.contains(listIter, p);
			}
			assertTrue(found);
		}
		// @ensures !hasPrevious();
		assertFalse(result.hasPrevious());
		// @ensures lastIndex() == -1;
		assertTrue(result.lastIndex() == -1);
		// @ensures comparator() != null;
		assertNotNull(result.comparator());
		// @ensures lastIterator() == null;
		assertNull(result.lastIterator());
		// @ensures (\forall I iter; iters.contains(iter); !iter.hasPrevious() && iter.lastIndex() == -1);
		for (User u : postIterators) {
			assertFalse(u.hasPrevious());
			assertTrue(u.lastIndex() == -1);
		}
		
		// Invariant:
		assertInvariant(result);
	}

    /**
     * Test method for method startIteration
     *
     * (Re)Initialise ce ListIterateur pour le démarrage d'une nouvelle itération
     * sur ses éléments.
     */
    @ParameterizedTest
    @MethodSource("fusSortedIterProvider")
    public void teststartIteration(FusionSortedIterator<Post, User> self) {
            assumeTrue(self != null);
    		setModel(self);
            
            // Invariant:
            assertInvariant(self);
            
            // Pré-conditions:
            
            
            // Oldies:
            
            // Exécution:
            self.startIteration();
            
            // Post-conditions:
            // @ensures !hasPrevious();
            assertFalse(self.hasPrevious());
            // @ensures previousIndex() == -1;
            assertEquals(-1, self.previousIndex());
            // @ensures nextIndex() == 0;
            assertEquals(0, self.nextIndex());
            // @ensures lastIndex() == -1;
            assertEquals(-1, self.lastIndex());
            // @ensures lastIterator() == null;
            assertNull(self.lastIterator());
            
            // Invariant:
            assertInvariant(self);
    }
    
    /**
     * Test method for method comparator
     *
     * Renvoie le comparateur selon lequel les éléments de cet itérateur sont
     * ordonnés.
     */
    @ParameterizedTest
    @MethodSource("fusSortedIterProvider")
    public void testcomparator(FusionSortedIterator<Post, User> self) {
            assumeTrue(self != null);
    		setModel(self);
            
            // Invariant:
            assertInvariant(self);
            
            // Pré-conditions:
            
            
            // Save state for purity check:
            saveState(self);
            
            // Oldies:
            
            // Exécution:
            Comparator<? super Post> result = self.comparator();
            
            // Post-conditions:
            // @ensures \result != null;
            assertNotNull(result);
            
            // Assert purity:
            assertPurity(self);
            
            // Invariant:
            assertInvariant(self);
    }
    

    /**
     * Test method for method lastIterator
     *
     * Renvoie l'itérateur ayant produit l'élément lors du dernier appel à next() ou
     * previous().
     */
    @ParameterizedTest
    @MethodSource("fusSortedIterProvider")
    public void testlastIterator(FusionSortedIterator<Post, User> self) {
            assumeTrue(self != null);
    		setModel(self);
            
            // Invariant:
            assertInvariant(self);
            
            // Pré-conditions:
            
            
            // Save state for purity check:
            saveState(self);
            
            // Oldies:
            
            // Exécution:
            User result = self.lastIterator();
            
            // Post-conditions:
            
            // Assert purity:
            assertPurity(self);
            
            // Invariant:
            assertInvariant(self);
    }
    
    /**
     * Test method for method lastIndex
     *
     * Renvoie l'index pour cet itérateur du dernier élément retourné par next() ou
     * previous().
     */
    @ParameterizedTest
    @MethodSource("fusSortedIterProvider")
    public void testlastIndex(FusionSortedIterator<Post, User> self) {
            assumeTrue(self != null);
    		setModel(self);
            
            // Invariant:
            assertInvariant(self);
            
            // Pré-conditions:
            
            
            // Save state for purity check:
            saveState(self);
            
            // Oldies:
            
            // Exécution:
            int result = self.lastIndex();
            
            // Post-conditions:
            
            // Assert purity:
            assertPurity(self);
            
            // Invariant:
            assertInvariant(self);
    }
       
    /**
	 * Test method for method hasNext
	 *
	 *
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testhasNext(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasNext();

		// Post-conditions:
		// @ensures !\result <==> nextIndex() == iterModel.size();
		assertEquals(self.nextIndex() == iterModel.size(), !result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method next
	 *
	 * Renvoie l'élément suivant et avance le curseur.
	 *
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément suivant
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testnext(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		if (!self.hasNext()) {
			assertThrows(NoSuchElementException.class, () -> self.next());
			return;
		}

		// Oldies:
		// old in:@ensures previousIndex() == \old(nextIndex());
		int oldNextIndex = self.nextIndex();
		// old in:@ensures \result == \old(iterModel.get(nextIndex()));
		Post oldNext = iterModel.get(self.nextIndex());
		// old in:@ensures \old(hasPrevious()) ==>
		// !iterModel.get(\old(previousIndex())).before(\result);
		boolean oldHasPrevious = self.hasPrevious();
		int oldPrevIndex = self.previousIndex();
		// old in:@ensures nextIndex() == \old(nextIndex() + 1);
		// old in:@ensures previousIndex() == \old(previousIndex() + 1);

		// Exécution:
		Post result = self.next();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures lastIterator() != null;
		assertNotNull(self.lastIterator());
		// @ensures \result.equals(lastIterator().getPrevious());
		assertEquals(self.lastIterator().getPrevious(), result);
		// @ensures hasPrevious();
		assertTrue(self.hasPrevious());
		// @ensures \result.equals(iterModel.get(previousIndex()));
		assertEquals(iterModel.get(self.previousIndex()), result);
		// @ensures previousIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.previousIndex());
		// @ensures \result.equals(\old(iterModel.get(nextIndex())));
		assertEquals(oldNext, result);
		// @ensures \old(hasPrevious()) ==> comparator().compare(iterModel.get(\old(previousIndex())), result) <= 0;
		if (oldHasPrevious) {
			assertTrue(self.comparator().compare(iterModel.get(oldPrevIndex), result) <= 0);
		}
		// @ensures nextIndex() == \old(nextIndex() + 1);
		assertEquals(oldNextIndex + 1, self.nextIndex());
		// @ensures previousIndex() == \old(previousIndex() + 1);
		assertEquals(oldPrevIndex + 1, self.previousIndex());
		// @ensures lastIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.lastIndex());
		
		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasPrevious
	 *
	 *
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testhasPrevious(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasPrevious();

		// Post-conditions:
		// @ensures !\result <==> previousIndex() == -1;
		assertEquals(self.previousIndex() == -1, !result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previous
	 *
	 * Renvoie l'élément précédent et recule le curseur.
	 *
	 * @throws NoSuchElementException si l'itérateur n'a pas d'élément précédent
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testprevious(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		if (!self.hasPrevious()) {
			assertThrows(NoSuchElementException.class, () -> self.previous());
			return;
		}

		// Oldies:
		// old in:@ensures \result == \old(iterModel.get(previousIndex()));
		Post oldPrevious = iterModel.get(self.previousIndex());
		// old in:@ensures \old(hasNext()) ==>
		// !\result.after(iterModel.get(\old(nextIndex()));
		boolean oldHasNext = self.hasNext();
		int oldNextIndex = self.nextIndex();
		// old in:@ensures previousIndex() == \old(previousIndex() - 1);
		int oldPrevIndex = self.previousIndex();
		// old in:@ensures nextIndex() == \old(nextIndex() - 1);

		// Exécution:
		Post result = self.previous();

		// Post-conditions:
		// @ensures hasNext();
		assertTrue(self.hasNext());
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures lastIterator() != null;
		assertNotNull(self.lastIterator());
		// @ensures \result.equals(lastIterator().getNext());
		assertEquals(self.lastIterator().getNext(), result);
		// @ensures \result.equals(\old(iterModel.get(previousIndex())));
		assertEquals(oldPrevious, result);
		// @ensures \result.equals(iterModel.get(nextIndex()));
		assertEquals(iterModel.get(self.nextIndex()), result);
		// @ensures \old(hasNext()) ==> comparator().compare(\result, iterModel.get(\old(nextIndex())) <= 0;
		if (oldHasNext) {
			assertTrue(self.comparator().compare(result, iterModel.get(oldNextIndex)) <= 0);
		}
		// @ensures previousIndex() == \old(previousIndex() - 1);
		assertEquals(oldPrevIndex - 1, self.previousIndex());
		// @ensures nextIndex() == \old(nextIndex() - 1);
		assertEquals(oldNextIndex - 1, self.nextIndex());
		// @ensures lastIndex() == \old(previousIndex());
		assertEquals(oldPrevIndex, self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method nextIndex
	 *
	 *
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testnextIndex(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.nextIndex();

		// Post-conditions:
		// @ensures hasNext() <==> \result >= 0 && \result < iterModel.size();
		assertEquals(self.hasNext(), result >= 0 && result < iterModel.size());
		// @ensures !hasNext() <==> \result == iterModel.size();
		assertEquals(!self.hasNext(), result == iterModel.size());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previousIndex
	 *
	 *
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testpreviousIndex(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.previousIndex();

		// Post-conditions:
		// @ensures hasPrevious() <==> \result >= 0 && \result < iterModel.size();
		assertEquals(self.hasPrevious(), result >= 0 && result < iterModel.size());
		// @ensures !hasPrevious() <==> \result == -1;
		assertEquals(!self.hasPrevious(), result == -1);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method remove
	 *
	 *
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("fusSortedIterProvider")
	public void testremove(FusionSortedIterator<Post, User> self) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		assertThrows(UnsupportedOperationException.class, () -> self.remove());

		// Post-conditions:

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method set
	 *
	 *
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("iterAndPostProvider")
	public void testset(FusionSortedIterator<Post, User> self, Post e) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		assertThrows(UnsupportedOperationException.class, () -> self.set(e));

		// Post-conditions:

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method add
	 *
	 *
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("iterAndPostProvider")
	public void testadd(FusionSortedIterator<Post, User> self, Post e) {
		assumeTrue(self != null);
		setModel(self);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		assertThrows(UnsupportedOperationException.class, () -> self.add(e));

		// Post-conditions:

		// Invariant:
		assertInvariant(self);
	}
}
