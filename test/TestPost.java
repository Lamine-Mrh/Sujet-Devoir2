
package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import social.model.Post;
import social.model.RePost;
import social.model.SimplePost;
import social.model.User;

/**
 * Test class for Post.
 *
 * Un post d'un utilisateur d'un réseau social, caractérisé par son texte, son
 * auteur, sa date de création et l'ensemble des utilisateurs l'ayant "liké".
 * 
 * La seule caractéristque modifiable de cette classe est l'ensemble des
 * utilisteurs ayant "liké" ce Post.
 */
public class TestPost {

	public static Stream<Post> PostProvider() {
		return Stream.generate(DataProvider::postSupplier).limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> postAndUserProvider() {
		Stream<Arguments> exStream = Stream.of(Arguments.of(DataProvider.postSupplier(), null));
		return Stream
				.concat(exStream,
						Stream.generate(() -> Arguments.of(DataProvider.postSupplier(), DataProvider.userSupplier())))
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> postAndPostProvider() {
		Stream<Arguments> exStream = Stream.of(Arguments.of(DataProvider.postSupplier(), null));
		return Stream
				.concat(exStream,
						Stream.generate(() -> Arguments.of(DataProvider.postSupplier(), DataProvider.postSupplier())))
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> stringAndUserProvider() {
		return Stream
				.concat(Stream.of(Arguments.of(DataProvider.stringSupplier(), null), Arguments.of(null, null)),
						Stream.generate(() -> Arguments.of(DataProvider.stringSupplier(), DataProvider.userSupplier())))
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> stringAndUserAndPostProvider() {
		Stream<Arguments> mainStream = Stream.generate(() -> DataProvider.userSupplier())
				.map(u -> Arguments.of(DataProvider.stringSupplier(), u,
						DataProvider.randBool(50) ? DataProvider.postSupplier()
								: DataProvider.getRandomElt(u.getPosts())));
		return Stream.concat(
				Stream.of(Arguments.of(DataProvider.stringSupplier(), null, DataProvider.postSupplier()),
						Arguments.of(null, null, null), Arguments.of(null, DataProvider.userSupplier(), null)),
				mainStream)
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<String> stringProvider() {
		return Stream.generate(() -> DataProvider.stringSupplier()).limit(DataProvider.LG_STREAM);
	}

	private String text;
	private Instant creationDate;
	private Set<User> likers;

	private void saveState(Post self) {
		text = self.getText();
		creationDate = self.getDate();
		likers = new HashSet<User>(self.getLikers());
	}

	private void assertPurity(Post self) {
		assertEquals(text, self.getText());
		assertEquals(creationDate, self.getDate());
		assertEquals(likers, self.getLikers());
	}

	public void assertInvariant(Post self) {
		// Put here the code to check the invariant:
		// @invariant getText() != null;
		assertNotNull(self.getText());
		// @invariant getDate() != null;
		assertNotNull(self.getDate());
		// @invariant getLikers() != null && !getLikers().contains(null);
		assertNotNull(self.getLikers());
		assertFalse(self.getLikers().contains(null));
		// @invariant iterator() != null;
		assertNotNull(self.iterator());
	}

	/**
	 * Test method for constructor SimplePost
	 *
	 * Initialise un nouveau Post ayant pour text la chaine de caractères spécifiée.
	 * La date de ce nouveau Post est la date courante au moment de l'exécution de
	 * ce constructeur.
	 */
	@ParameterizedTest
	@MethodSource("stringProvider")
	public void testSimplePost(String text) {

		// Pré-conditions:
		// @requires text != null;
		if (text == null) {
			assertThrows(NullPointerException.class, () -> new SimplePost(text));
			return;
		}

		// Oldies:
		Instant oldDate = Instant.now();

		// Exécution:
		SimplePost result = new SimplePost(text);

		// Post-conditions:
		// @ensures getText().equals(text);
		assertEquals(text, result.getText());
		// @ensures oldDate.compareTo(getDate()) <= 0;
		assertTrue(oldDate.compareTo(result.getDate()) <= 0);
		// @ensures getDate().compareTo(Instant.now()) <= 0;
		assertTrue(result.getDate().compareTo(Instant.now()) <= 0);
		// @ensures getLikers().isEmpty();
		assertTrue(result.getLikers().isEmpty());

		// Invariant:
		assertInvariant(result);
	}

	@ParameterizedTest
	@MethodSource("stringAndUserAndPostProvider")
	public void testRePost(String text, User u, Post p) {

		// Pré-conditions:
		// @requires text != null && u != null && p != null;
		boolean hasNullArg = text == null || u == null || p == null;
		// @requires u.getPosts().contains(p);
		boolean invalidArg = u != null && !u.getPosts().contains(p);
		if (hasNullArg || invalidArg) {
			Throwable ex = assertThrows(Throwable.class, () -> new RePost(text, u, p));
			if (ex instanceof NullPointerException) {
				assertTrue(hasNullArg);
				return;
			}
			if (ex instanceof IllegalArgumentException) {
				assertTrue(invalidArg);
				return;
			}
			fail("Exception inatendue");
			return;
		}

		// Oldies:
		Instant oldDate = Instant.now();

		// Exécution:
		RePost result = new RePost(text, u, p);

		// Post-conditions:
		// @ensures getText().contains(text);
		assertTrue(result.getText().contains(text));
		// @ensures getText().contains(u.getName());
		assertTrue(result.getText().contains(u.getName()));
		// @ensures getText().contains(p.getText());
		assertTrue(result.getText().contains(p.getText()));
		// @ensures p.getDate().compareTo(getDate()) <= 0;
		assertTrue(p.getDate().compareTo(result.getDate()) <= 0);
		// @ensures oldDate.compareTo(getDate()) <= 0;
		assertTrue(oldDate.compareTo(result.getDate()) <= 0);
		// @ensures getDate().compareTo(Instant.now()) <= 0;
		assertTrue(result.getDate().compareTo(Instant.now()) <= 0);
		// @ensures getLikers().isEmpty();
		assertTrue(result.getLikers().isEmpty());

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getDate
	 *
	 * Renvoie une nouvelle instance de Date représentant la date de création de ce
	 * Post.
	 */
	@ParameterizedTest
	@MethodSource("PostProvider")
	public void testgetDate(Post self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Instant result = self.getDate();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getText
	 *
	 * Renvoie le texte de ce Post.
	 */
	@ParameterizedTest
	@MethodSource("PostProvider")
	public void testgetText(Post self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getText();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getLikeNumber
	 *
	 * Renvoie le nombre de like, c'est à dire le nombre d'utilisateurs ayant "liké"
	 * ce Post.
	 */
	@ParameterizedTest
	@MethodSource("PostProvider")
	public void testgetLikeNumber(Post self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getLikeNumber();

		// Post-conditions:
		// @ensures \result == getLikers().size();
		assertEquals(self.getLikers().size(), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasLikeFrom
	 *
	 * Renvoie true si l'utilisateur spécifié fait partie des "likers" de ce Post.
	 */
	@ParameterizedTest
	@MethodSource("postAndUserProvider")
	public void testhasLikeFrom(Post self, User u) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasLikeFrom(u);

		// Post-conditions:
		// @ensures \result <==> getLikers().contains(u);
		assertEquals(self.getLikers().contains(u), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method addLikeFrom
	 *
	 * Ajoute un utilisateur à l'ensemble des utilisateurs ayant "liké" ce message.
	 * L'auteur d'un Post a la possibilité de "liker" les messages dont il est
	 * l'auteur.
	 * 
	 * @throws NullPointerException si l'argument spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("postAndUserProvider")
	public void testaddLikeFrom(Post self, User u) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires u != null;
		if (u == null) {
			assertThrows(NullPointerException.class, () -> self.addLikeFrom(u));
			return;
		}

		// Oldies:
		// old in:@ensures \result <==> !\old(hasLikeFrom(u));
		boolean oldHasLike = self.hasLikeFrom(u);
		// old in:@ensures \result ==> (getLikeNumbre() == \old(getLikeNumber() + 1));
		// old in:@ensures !\result ==> (getLikeNumbre() == \old(getLikeNumber()));
		int oldLikeNb = self.getLikeNumber();

		// Exécution:
		boolean result = self.addLikeFrom(u);

		// Post-conditions:
		// @ensures hasLikeFrom(u);
		assertTrue(self.hasLikeFrom(u));
		// @ensures \result <==> !\old(hasLikeFrom(u));
		assertEquals(!oldHasLike, result);
		// @ensures \result ==> (getLikeNumber() == \old(getLikeNumber() + 1));
		if (result) {
			assertEquals(oldLikeNb + 1, self.getLikeNumber());
		} else {
			// @ensures !\result ==> (getLikeNumbre() == \old(getLikeNumber()));
			assertEquals(oldLikeNb, self.getLikeNumber());
		}
		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getLikers
	 *
	 * Renvoie une vue non modifiable de l'ensemble des "likers" de ce Post.
	 */
	@ParameterizedTest
	@MethodSource("PostProvider")
	public void testgetLikers(Post self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Set<User> result = self.getLikers();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		for (User u : User.getAllUser()) {
			// @ensures (\forall User u; hasLikeFrom(u); \result.contains(u));
			if (self.hasLikeFrom(u)) {
				assertTrue(result.contains(u));
			}
		}
		// @ensures (\forall User u; \result.contains(u); hasLikeFrom(u));
		for (User u : result) {
			assertTrue(self.hasLikeFrom(u));
		}
		// @ensures \result.size() == getLikeNumber();
		assertEquals(self.getLikeNumber(), result.size());

		DataProvider.assertIsUnmodifiable(result, () -> DataProvider.getRandomElt(User.getAllUser()));

		// purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method iterator
	 *
	 * Renvoie un iterateur sur l'ensemble des "likers" de ce Post. L'iterateur
	 * renvoyé interdit toute modification de l'ensemble.
	 */
	@ParameterizedTest
	@MethodSource("PostProvider")
	public void testiterator(Post self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ListIterator<User> result = self.iterator();

		// Post-conditions:
		// @ensures \resmodel = new ListIterObserverAdapter<User>(\result);
		ListIterObserver<User> resmodel = new ListIterObserverAdapter<User>(result);
		// @ensures \resmodel.toSet().equals(getLikers());
		assertEquals(self.getLikers(), resmodel.toSet());
		// @ensures (* L'iterateur renvoyé interdit toute modification *)
		DataProvider.assertIsUnmodifiable(result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method compareTo
	 *
	 * Compare ce Post avec le Post spécifié selon l'ordre de leurs dates de
	 * création.
	 * 
	 * En théorie, les dates représentées n'étant précises qu'à 1ms près, il
	 * pourrait arrivé que deux Post différents aient la même date et donc que la
	 * méthode renvoie 0 pour des Post différents, ce qui rendrait l'ordre défini
	 * par cette opérateur de comparaison non consistent avec equals (cf. interface
	 * Comparable<T>).
	 * 
	 * @throws NullPointerException si l'argument spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("postAndPostProvider")
	public void testcompareTo(Post self, Post p) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires p != null;
		if (p == null) {
			assertThrows(NullPointerException.class, () -> self.compareTo(p));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.compareTo(p);

		// Post-conditions:
		// @ensures \result == this.getDate().compareTo(p.getDate());
		assertEquals(self.getDate().compareTo(p.getDate()), result);
		// @ensures (\result == 0) <==> (!self.before(p) && !self.after(p));
		assertEquals((!self.isBefore(p) && !self.isAfter(p)), (result == 0));
		// @ensures (\result < 0) <==> this.before(p);
		assertEquals(self.isBefore(p), (result < 0));
		// @ensures (\result > 0) <==> this.after(p);
		assertEquals(self.isAfter(p), (result > 0));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method before
	 *
	 * Teste si ce Post a été publié avant le Post spécifié.
	 * 
	 * @throws NullPointerException si l'argument spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("postAndPostProvider")
	public void testbefore(Post self, Post p) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires p != null;
		if (p == null) {
			assertThrows(NullPointerException.class, () -> self.compareTo(p));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isBefore(p);

		// Post-conditions:
		// @ensures \result <==> this.getDate().isBefore(p.getDate());
		assertEquals(self.getDate().isBefore(p.getDate()), result);

		// @ensures \result <==> (this.compareTo(p) < 0);
		assertEquals(self.compareTo(p) < 0, result);
		// @ensures !(\result && this.after(p));
		assertFalse(result && self.isAfter(p));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method after
	 *
	 * Teste si ce Post a été publié après le Post spécifié.
	 * 
	 * @throws NullPointerException si l'argument spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("postAndPostProvider")
	public void testafter(SimplePost self, SimplePost p) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires p != null;
		if (p == null) {
			assertThrows(NullPointerException.class, () -> self.compareTo(p));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.isAfter(p);

		// Post-conditions:
		// @ensures \result <==> this.getDate().isAfter(p.getDate());
		assertEquals(self.getDate().isAfter(p.getDate()), result);
		// @ensures \result <==> (this.compareTo(p) > 0);
		assertEquals(self.compareTo(p) > 0, result);
		// @ensures !(\result && this.isBefore(p));
		assertFalse(result && self.isBefore(p));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for Post
