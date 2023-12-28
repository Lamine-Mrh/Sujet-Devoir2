package social.model.test;

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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import social.model.FusionSortedIterator;
import social.model.Post;
import social.model.SimplePost;
import social.model.User;
import static social.model.test.DataProvider.getRandomElt;


/**
 * Test class for User.
 *
 * Un utilisateur du réseau social Microdon. Chaque instance possède un nom
 * (getName()) unique, dans ce but chaque instance créée est mémorisée dans un
 * attribut static et le constructeur garantit qu'une nouvelle instance ne
 * peut-être créée avec un nom déjà porté par une instance pré-existante. En
 * plus de son nom, chaque User est caractérisé par son mot de passe, la liste
 * de ses messages, l'ensemble des User auxquels il est abonné et l'ensemble des
 * User qui sont abonnés à son compte (i.e. ses followers).
 * 
 * Un ensemble complet de méthodes permet d'utiliser un User comme un
 * ListIterator afin d'effectuer des itérations bidirectionnelles sur la liste
 * des Post de l'User: ce sont les méthodes: startIteration(), hasNext(),
 * nextIndex(), next(), getNext(), hasPrevious(), previousIndex(), previous(),
 * getPrevious() et lastIndex().
 * 
 * <pre>{@code
 * aUser.startIteration(); // Initialisation pour une nouvelle itération
 * // Affichage des Post du plus récent au plus ancien
 * while (aUser.hasNext()) {
 * 	System.out.println("Post suivant (plus ancien):" + aUser.next());
 * }
 * // Affichage des Post du plus ancien au plus récent
 * while (aUser.hasPrevious()) {
 * 	System.out.println("Post précédent (plus récent):" + aUser.previous());
 * }
 * }</pre>
 */
public class TestUser {

	public static Stream<User> userProvider() {
		return Stream.generate(DataProvider::userSupplier).limit(DataProvider.LG_STREAM);
	}

	public static Stream<String> stringProvider() {
		return Stream.generate(DataProvider::stringSupplier).limit(DataProvider.LG_STREAM);

	}

	public static Stream<Arguments> stringAndStringProvider() {
		return Stream.generate(() -> Arguments.of(DataProvider.stringSupplier(), DataProvider.stringSupplier()))
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> userAndStringProvider() {
		return Stream.generate(() -> Arguments.of(DataProvider.userSupplier(), DataProvider.stringSupplier()))
				.limit(DataProvider.LG_STREAM);
	}

	public static Stream<Arguments> userAndPostProvider() {
		User u = DataProvider.userSupplier();
		if (u.getPostNb() == 0) {
			u.addPost(new SimplePost("Test msg 1"));
			u.addPost(new SimplePost("Test msg 2"));
		}
		Post p = getRandomElt(u.getPosts());
		Stream<Arguments> exStream = Stream.of(Arguments.of(u, p),
							Arguments.of(DataProvider.userSupplier(), p),
							Arguments.of(u, null));
		return Stream.concat(exStream,
				Stream.generate(() -> Arguments.of(DataProvider.userSupplier(),
													DataProvider.postSupplier())))
				.limit(DataProvider.LG_STREAM);
	}
	
	public static Stream<Arguments> userAndIntProvider() {
		return userProvider().map(u -> Arguments.of(u, DataProvider.randInt(-1, u.getPostNb() + 2)));
	}

	public static Stream<Arguments> userAndUserProvider() {
		User u = DataProvider.userSupplier();
		Stream<Arguments> exStream = Stream.of(Arguments.of(u, null), Arguments.of(u, u));
		return Stream
				.concat(exStream,
						Stream.generate(() -> Arguments.of(DataProvider.userSupplier(), DataProvider.userSupplier())))
				.limit(DataProvider.LG_STREAM);
	}

	private String name;
	private String password;
	private Instant subscriptionDate;
	private Set<User> subscriptions;
	private Set<User> followers;
	private List<Post> posts;
	private int lastIndex;
	private int previousIndex;
	private int nextIndex;

	private void saveState(User self) {
		// Put here the code to save the state of self:
		name = self.getName();
		password = self.getPassword();
		subscriptionDate = self.getRegistrationDate();
		subscriptions = new HashSet<User>(self.getSubscriptions());
		followers = new HashSet<User>(self.getFollowers());
		posts = new LinkedList<Post>(self.getPosts());
		lastIndex = self.lastIndex();
		previousIndex = self.previousIndex();
		nextIndex = self.nextIndex();
	}

	private void assertPurity(User self) {
		// Put here the code to check purity for self:
		assertEquals(self.getName(), name);
		assertEquals(self.getPassword(), password);
		assertEquals(self.getRegistrationDate(), subscriptionDate);
		assertEquals(self.getSubscriptions(), subscriptions);
		assertEquals(self.getFollowers(), followers);
		assertEquals(self.getPosts(), posts);
		assertEquals(self.lastIndex(), lastIndex);
		assertEquals(self.previousIndex(), previousIndex);
		assertEquals(self.nextIndex(), nextIndex);
	}

	public void assertInvariant(User self) {
		// Put here the code to check the invariant:
		// @invariant User.getAllUser() != null;
		assertNotNull(User.getAllUser());
		// @invariant User.getAllUser().contains(this);
		assertTrue(User.getAllUser().contains(self));
		// @invariant User.getUser(this.getName()) == this;
		assertTrue(User.getUser(self.getName()) == self);
		// @invariant User.hasUser(this.getName());
		assertTrue(User.hasUser(self.getName()));
		// @invariant User.isValidUserName(this.getName());
		assertTrue(User.isValidUserName(self.getName()));
		// @invariant User.isValidPassword(this.getPassword();
		assertTrue(User.isValidPassword(self.getPassword()));
		// @invariant getRegistrationDate() != null;
		assertNotNull(self.getRegistrationDate());
		// @invariant getSubscriptions() != null && !getSubscriptions().contains(null);
		assertNotNull(self.getSubscriptions());
		assertFalse(self.getSubscriptions().contains(null));
		// @invariant getSubscriptions().size() < User.getAllUser().size();
		assertTrue(self.getSubscriptions().size() < User.getAllUser().size());
		// @invariant getFollowers() != null && !getFollowers().contains(null);
		assertNotNull(self.getFollowers());
		assertFalse(self.getFollowers().contains(null));
		// @invariant getFollowers().size() < User.getAllUser().size();
		assertTrue(self.getFollowers().size() < User.getAllUser().size());
		// @invariant getPosts() != null && !getPosts().contains(null);
		assertNotNull(self.getPosts());
		assertFalse(self.getPosts().contains(null));
		// @invariant newsFeed() != null;
		assertNotNull(self.newsFeed());
		// @invariant iterator() != null;
		assertNotNull(self.iterator());
		// @invariant previousIndex() >= -1 && previousIndex() < getPostNb();
		assertTrue(self.previousIndex() >= -1);
		assertTrue(self.previousIndex() < self.getPostNb());
		// @invariant nextIndex() >= 0 && nextIndex() <= getPostNb();
		assertTrue(self.nextIndex() >= 0);
		assertTrue(self.nextIndex() <= self.getPostNb());
		// @invariant lastIndex() >= -1 && lastIndex() < getPostNb();
		assertTrue(self.lastIndex() >= -1);
		assertTrue(self.lastIndex() < self.getPostNb());
		// @invariant !hasPrevious() <==> previousIndex() == -1;
		assertEquals(!self.hasPrevious(), self.previousIndex() == -1);
		// @invariant !hasNext() <==> nextIndex() == getPostNb();
		assertEquals(!self.hasNext(), self.nextIndex() == self.getPostNb());
		// @invariant nextIndex() == previousIndex() + 1;
		assertEquals(self.nextIndex(), self.previousIndex() + 1);
		// @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex();
		assertTrue(self.lastIndex() == self.nextIndex() || self.lastIndex() == self.previousIndex());
		
		ListIterObserver<Post> resmodel = new ListIterObserverAdapter<Post>(self);
		// @invariant \result != null && !\resmodel.contains(null);
		assertFalse(resmodel.contains​(null));
		// @invariant \resmodel.toList().equals(getPosts());
		assertEquals(self.getPosts(), resmodel.toList());
		// @invariant \resmodel.isSorted(Comparator.reverseOrder());		
		assertTrue(resmodel.isSorted(Comparator.reverseOrder()));	}

	/**
	 * Test method for constructor User
	 *
	 * Initialise une nouvelle instance ayant les nom et mot de passe spécifiés. Le
	 * nom spécifié ne doit pas déjà être le nom d'une instance existante. La
	 * nouvelle instance est mémorisée dans une varaible static de cette classe. La
	 * date d'inscription du nouvel utilisateur est la date au moment de l'exécution
	 * de ce constructeur.
	 * 
	 * @throws NullPointerException     si le nom ou le mot de passe spécifié est
	 *                                  null
	 * @throws IllegalArgumentException si le nom ou le mot de passe spécifié n'est
	 *                                  pas valide ou si le nom spécifié a déjà été
	 *                                  donné à une autre instance
	 */
	@ParameterizedTest
	@MethodSource("stringAndStringProvider")
	public void testUser(String userName, String password) {

		// Pré-conditions:
		// @requires userName != null && password != null;
		boolean nullPointer = (userName == null || password == null);
		boolean invalideArg = false;
		// @requires User.isValidUserName(userName) && User.isValidPassword(password);
		// @requires !User.hasUser(userName);
		if (!nullPointer) {
			invalideArg = (!User.isValidUserName(userName) || !User.isValidPassword(password)
					|| User.hasUser(userName));
		}
		if (nullPointer || invalideArg) {
			Throwable e = assertThrows(Throwable.class, () -> new User(userName, password));
			if (e instanceof NullPointerException) {
				assertTrue(nullPointer);
				return;
			}
			if (e instanceof IllegalArgumentException) {
				assertTrue(invalideArg);
				return;
			}
			fail("Exception inattendue: " + e);
			return;
		}

		// Oldies:
		Instant oldDate = Instant.now();

		// Exécution:
		User result = new User(userName, password);

		// Post-conditions:
		// @ensures getName().equals(userName);
		assertEquals(userName, result.getName());
		// @ensures getPassword().equals(password);
		assertEquals(password, result.getPassword());
		// @ensures getSubscriptionDate() != null;
		assertNotNull(result.getRegistrationDate());
		// @ensures oldDate.compareTo(getSubscriptionDate()) <= 0;
		assertTrue(oldDate.compareTo(result.getRegistrationDate()) <= 0);
		// @ensures getSubscriptionDate().compareTo(Instant.now()) <= 0;
		assertTrue(result.getRegistrationDate().compareTo(Instant.now()) <= 0);
		// @ensures getSubscriptions().isEmpty();
		assertNotNull(result.getSubscriptions());
		assertTrue(result.getSubscriptions().isEmpty());
		// @ensures getPosts() != null;
		assertNotNull(result.getPosts());
		// @ensures getPosts().isEmpty();
		assertTrue(result.getPosts().isEmpty());
		// @ensures getFollowers() != null;
		assertNotNull(result.getFollowers());
		// @ensures getFollowers().isEmpty();
		assertTrue(result.getFollowers().isEmpty());
		// @ensures User.getAllUser().contains(this);
		assertTrue(User.getAllUser().contains(result));
		// @ensures User.getUser(userName) == this;
		assertSame(User.getUser(userName), result);
		// @ensures User.hasUser(userName);
		assertTrue(User.hasUser(userName));

		// Invariant:
		assertInvariant(result);
	}

	/**
	 * Test method for method getName
	 *
	 * Renvoie le nom de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetName(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getName();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPassword
	 *
	 * Renvoie le mot de passe de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetPassword(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		String result = self.getPassword();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getRegistrationDate
	 *
	 * Renvoie une nouvelle instance de Instant représentant la date d'inscription
	 * de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetRegistrationDate(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Instant result = self.getRegistrationDate();

		// Post-conditions:

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getSubscriptions
	 *
	 * Renvoie une vue non modifiable de l'ensemble des utilisateurs auxquels cet
	 * utilisateur s'est abonné.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetSubscriptions(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Set<User> result = self.getSubscriptions();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures User.getAllUser().containsAll(\result);
		assertTrue(User.getAllUser().containsAll(result));
		// @ensures (\forall User u; \result.contains(u); hasSubscritionTo(u));
		for (User u : result) {
			assertTrue(self.hasSubscriptionTo(u));
		}
		// @ensures (\forall User u; \result.contains(u); u.hasFollower(this));
		for (User u : result) {
			assertTrue(u.hasFollower(self));
		}
		DataProvider.assertIsUnmodifiable(result, () -> getRandomElt(User.getAllUser()));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getFollowers
	 *
	 * Renvoie une vue non modifiable de l'ensemble des utilisateurs abonnés à cet
	 * utilisateur (ses "followers").
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetFollowers(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Set<User> result = self.getFollowers();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures User.getAllUser().containsAll(\result);
		assertTrue(User.getAllUser().containsAll(result));
		for (User u : result) {
			// @ensures (\forall User u; \result.contains(u); this.hasFollower(u));
			assertTrue(self.hasFollower(u));
			// @ensures (\forall User u; \result.contains(u); u.hasSubscriptionTo(this));
			assertTrue(u.hasSubscriptionTo(self));
		}
		DataProvider.assertIsUnmodifiable(result, () -> getRandomElt(User.getAllUser()));

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method addSubscriptionTo
	 *
	 * Ajoute l'utilisateur spécifié à l'ensemble des souscriptions de cet
	 * utilisateur. Un utilisateur ne peut souscrire à lui-même. Renvoie true si
	 * l'utilisateur spécifié ne faisait pas déjà partie des souscriptions de cet
	 * utilisateur. Cet utilisateur est ajouté à l'ensemble des utilisateurs abonnés
	 * à l'utilisateur spécifié.
	 * 
	 * @throws NullPointerException     si l'argument spécifié est null
	 * @throws IllegalArgumentException si l'argument spécifié est cet utilisateur
	 */
	@ParameterizedTest
	@MethodSource("userAndUserProvider")
	public void testaddSubscriptionTo(User self, User u) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires u != null;
		// @requires !this.equals(u);
		if (u == null || self.equals(u)) {
			Throwable ex = assertThrows(Throwable.class, () -> self.addSubscriptionTo(u));
			if (ex instanceof NullPointerException) {
				assertNull(u);
				return;
			}
			if (ex instanceof IllegalArgumentException) {
				assertEquals(self, u);
				return;
			}
			fail("Exception inattendue: " + ex);
			return;
		}

		// Oldies:
		// old in:@ensures \result <==> !\old(hasSubscriptionTo(u));
		boolean oldHasSub = self.hasSubscriptionTo(u);
		// old in:@ensures \result <==> (getSubscriptionNb() == \old(getSubscriptionNb()
		// + 1));
		// old in:@ensures !\result <==> (getSubscriptionNb() ==
		// \old(getSubscriptionNb()));
		int oldSubNb = self.getSubscriptionNb();
		// old in:@ensures \result <==> u != null && (u.getFollowerNb() ==
		// \old(u.getFollowerNb() + 1));
		// old in:@ensures !\result <==> u == null || (u.getFollowerNb() ==
		// \old(u.getFollowerNb()));
		int oldFollowNb = u.getFollowerNb();
		// old in:@ensures !\result <==>
		// (getSubscriptions().equals(\old(getSubscriptions()));
		Set<User> oldSubSet = new HashSet<User>(self.getSubscriptions());
		// old in:@ensures !\result <==> u == null ||
		// (u.getFollowers().equals(\old(u.getFollowers()));
		Set<User> oldFollowSet = new HashSet<User>(u.getFollowers());

		// Exécution:
		boolean result = self.addSubscriptionTo(u);

		// Post-conditions:
		// @ensures hasSubscriptionTo(u);
		assertTrue(self.hasSubscriptionTo(u));
		// @ensures u.hasFollower(this);
		assertTrue(u.hasFollower(self));
		// @ensures \result <==> !\old(hasSubscriptionTo(u));
		assertEquals(!oldHasSub, result);
		// @ensures \result <==> (getSubscriptionNb() == \old(getSubscriptionNb() + 1));
		assertEquals(self.getSubscriptionNb() == oldSubNb + 1, result);
		// @ensures \result <==> (u.getFollowerNb() == \old(u.getFollowerNb() + 1));
		assertEquals(u.getFollowerNb() == oldFollowNb + 1, result);
		// @ensures !\result <==> (getSubscriptionNb() == \old(getSubscriptionNb()));
		assertEquals(self.getSubscriptionNb() == oldSubNb, !result);
		// @ensures !\result <==> (u.getFollowerNb() == \old(u.getFollowerNb()));
		assertEquals(u.getFollowerNb() == oldFollowNb, !result);
		// @ensures !\result <==> (getSubscriptions().equals(\old(getSubscriptions()));
		assertEquals(self.getSubscriptions().equals(oldSubSet), !result);
		// @ensures !\result <==> (u.getFollowers().equals(\old(u.getFollowers()));
		assertEquals(u.getFollowers().equals(oldFollowSet), !result);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method removeSubscriptionTo
	 *
	 * Retire l'utilisateur spécifié de l'ensemble des abonnements de cet
	 * utilisateur. Renvoie true si l'utilisateur spécifié était précédemment un
	 * abonnement de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userAndUserProvider")
	public void testremoveSubscriptionTo(User self, User u) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:
		// old in:@ensures \result <==> \old(hasSubscriptionTo(u));
		boolean oldHasSub = self.hasSubscriptionTo(u);
		// old in:@ensures \result <==> u != null && \old(u.hasFollower(this));
		boolean oldHasFollow = false;
		if (u != null) {
			oldHasFollow = u.hasFollower(self);
		}
		// old in:@ensures \result <==> (getSubscriptionNb() == \old(getSubscriptionNb()
		// - 1));
		// old in:@ensures !\result <==> (getSubscriptionNb() ==
		// \old(getSubscriptionNb()));
		int oldSubNb = self.getSubscriptionNb();
		// old in:@ensures \result <==> u != null && (u.getFollowerNb() ==
		// \old(u.getFollowerNb()) - 1);
		// old in:@ensures !\result <==> u == null || (u.getFollowerNb() ==
		// \old(u.getFollowerNb()));
		int oldFollowNb = 0;
		if (u != null) {
			oldFollowNb = u.getFollowerNb();
		}
		// old in:@ensures !\result <==>
		// (getSubscriptions().equals(\old(getSubscriptions()));
		Set<User> oldSubSet = new HashSet<User>(self.getSubscriptions());
		// old in:@ensures !\result <==> u == null ||
		// (u.getFollowers().equals(\old(u.getFollowers()));
		Set<User> oldFollowSet = Collections.emptySet();
		if (u != null) {
			oldFollowSet = new HashSet<User>(u.getFollowers());
		}

		// Exécution:
		boolean result = self.removeSubscriptionTo(u);

		// Post-conditions:
		// @ensures !hasSubscriptionTo(u);
		assertFalse(self.hasSubscriptionTo(u));
		// @ensures u == null || !u.hasFollower(this);
		assertTrue(u == null || !u.hasFollower(self));
		// @ensures \result <==> \old(hasSubscriptionTo(u));
		assertEquals(oldHasSub, result);
		// @ensures \result <==> u != null && \old(u.hasFollower(this));
		assertEquals(u != null && oldHasFollow, result);
		// @ensures \result <==> (getSubscriptionNb() == \old(getSubscriptionNb() - 1));
		assertEquals(self.getSubscriptionNb() == oldSubNb - 1, result);
		// @ensures \result <==> u != null && (u.getFollowerNb() ==
		// \old(u.getFollowerNb()) - 1);
		assertEquals(u != null && u.getFollowerNb() == oldFollowNb - 1, result);
		// @ensures !\result <==> (getSubscriptionNb() == \old(getSubscriptionNb()));
		assertEquals(self.getSubscriptionNb() == oldSubNb, !result);
		// @ensures !\result <==> u == null || (u.getFollowerNb() ==
		// \old(u.getFollowerNb()));
		assertEquals(u == null || u.getFollowerNb() == oldFollowNb, !result);
		// @ensures !\result <==> (getSubscriptions().equals(\old(getSubscriptions()));
		assertEquals(self.getSubscriptions().equals(oldSubSet), !result);
		// @ensures !\result <==> u == null ||
		// (u.getFollowers().equals(\old(u.getFollowers()));
		assertEquals(u == null || u.getFollowers().equals(oldFollowSet), !result);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasSubscriptionTo
	 *
	 * Renvoie true si l'utilisateur spécifié fait partie des abonnements de cet
	 * utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userAndUserProvider")
	public void testhasSubscriptionTo(User self, User u) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasSubscriptionTo(u);

		// Post-conditions:
		// @ensures \result <==> getSubscriptions().contains(u);
		assertEquals(self.getSubscriptions().contains(u), result);
		// @ensures \result <==> u != null && u.hasFollower(this);
		assertEquals(u != null && u.hasFollower(self), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasFollower
	 *
	 * Renvoie true si l'utilisateur spécifié fait partie des followers de cet
	 * utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userAndUserProvider")
	public void testhasFollower(User self, User u) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasFollower(u);

		// Post-conditions:
		// @ensures \result <==> getFollowers().contains(u);
		assertEquals(self.getFollowers().contains(u), result);
		// @ensures \result <==> u != null && u.hasSubscriptionTo(this);
		assertEquals(u != null && u.hasSubscriptionTo(self), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getSubscriptionNb
	 *
	 * Renvoie le nombre d'utilisateurs auxquels cet utilisateur est abonné.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetSubscriptionNb(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getSubscriptionNb();

		// Post-conditions:
		// @ensures \result == getSubscriptions().size();
		assertEquals(self.getSubscriptions().size(), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getFollowerNb
	 *
	 * Renvoie le nombre d'utilisateurs abonnés à cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetFollowerNb(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getFollowerNb();

		// Post-conditions:
		// @ensures \result == getFollowers().size();
		assertEquals(self.getFollowers().size(), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPost
	 *
	 * Renvoie le ième plus récent Post de ce User.
	 * 
	 * @throws IndexOutOfBoundsException si l'index spécifié est < 0 ou >=
	 *                                   getPostNb()
	 */
	@ParameterizedTest
	@MethodSource("userAndIntProvider")
	public void testgetPost(User self, int i) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires i >= 0 && i < getPostNb();
		if (i < 0 || i >= self.getPostNb()) {
			assertThrows(IndexOutOfBoundsException.class, () -> self.getPost(i));
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Post result = self.getPost(i);

		// Post-conditions:
		// @ensures \result.equals(getPosts().get(i));
		assertEquals(self.getPosts().get(i), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPosts
	 *
	 * Renvoie une vue non modifiable de la liste des posts de cet utilisateur. La
	 * liste renvoyée est triée selon leurs dates, les messages les plus récents
	 * étant en tête de liste.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetPosts(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		List<Post> result = self.getPosts();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures (\forall int i, j; i >= 0 && i < j && j < \result.size();
		// \result.get(i).isAfter(\result.get(j)));
		if (result.size() >= 2) {
			Iterator<Post> iter = result.iterator();
			Post pred = iter.next();
			while (iter.hasNext()) {
				Post next = iter.next();
				assertTrue(pred.isAfter(next));
			}
		}
		Post p;
		if (result.isEmpty()) {
			p = new SimplePost(" ");
		} else {
			p = getRandomElt(result);
		}
		DataProvider.assertIsUnmodifiable(result, () -> p);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method addPost
	 *
	 * Ajoute le Post spécifié en tête de la liste des posts de cet utilisateur.
	 * 
	 * @throws NullPointerException si l'argument spécifié est null
	 */
	@ParameterizedTest
	@MethodSource("userAndPostProvider")
	public void testaddPost(User self, Post p) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires p != null;
		if (p == null) {
			assertThrows(NullPointerException.class, () -> self.addPost(p));
			return;
		}
		// @requires getPostNb() > 0 ==> p.isAfter(getPost(0));
		if (self.getPostNb() > 0 && !p.isAfter(self.getPost(0))) {
			assertThrows(IllegalArgumentException.class, () -> self.addPost(p));
			return;
		}

		// Oldies:
		// old in:@ensures getPostNb() == \old(getPostNb()) + 1;
		int oldPostNb = self.getPostNb();
		// old in:@ensures \old(lastIndex()) == -1 ==> nextIndex() == \old(nextIndex());
		// old in:@ensures \old(lastIndex()) == -1 ==> lastIndex() == -1;
		// old in:@ensures \old(lastIndex()) > -1 ==> nextIndex() == \old(nextIndex()) +
		// 1;
		// old in:@ensures \old(lastIndex()) > -1 ==> lastIndex() == \old(lastIndex()) +
		// 1;
		int oldNextIndex = self.nextIndex();
		int oldLastIndex = self.lastIndex();

		// Exécution:
		Post result = self.addPost(p);

		// Post-conditions:
		// @ensures \result == p;
		assertSame(p, result);
		// @ensures getPost(0).equals(\result);
		assertEquals(self.getPost(0), result);
		if (self.getPostNb() > 1) {
			assertTrue(result.isAfter(self.getPost(1)));
		}
		// @ensures getPostNb() == \old(getPostNb()) + 1;
		assertEquals(oldPostNb + 1, self.getPostNb());
		if (oldLastIndex == -1) {
			// @ensures \old(lastIndex()) == -1 ==> nextIndex() == \old(nextIndex());
			assertEquals(oldNextIndex, self.nextIndex());
			// @ensures \old(lastIndex()) == -1 ==> lastIndex() == -1;
			assertEquals(-1, self.lastIndex());
		} else {
			// @ensures \old(lastIndex()) > -1 ==> nextIndex() == \old(nextIndex()) + 1;
			assertEquals(oldNextIndex + 1, self.nextIndex());
			// @ensures \old(lastIndex()) > -1 ==> lastIndex() == \old(lastIndex()) + 1;
			assertEquals(oldLastIndex + 1, self.lastIndex());
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPostNb
	 *
	 * Renvoie le nombre de Post de cet utilisateur.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetPostNb(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.getPostNb();

		// Post-conditions:
		// @ensures \result == getPosts().size();
		assertEquals(self.getPosts().size(), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method newsFeed
	 *
	 * Renvoie un NewsFeed de cet utilisateur. Ce NewsFeed interdit toute
	 * modification, il est obtenu en fusionnant les listes de Post de cet
	 * utilisateur et des utilisateurs auxquels il est abonné, il utilise les
	 * itérateurs natifs des User. L'appel de cette méthode réinitialise les
	 * itérateurs natifs de ce User ainsi que ceux des User auxquels il est abonné,
	 * comme le ferait un appel à startIteration(). Ce NewsFeed énumère les Post par
	 * ordre de date du plus récent au plus ancien.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testnewsFeed(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Oldies:

		// Exécution:
		FusionSortedIterator<Post, User> result = self.newsFeed();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures lastIndex() == -1 && nextIndex() == 0 && previousIndex() == -1;
		assertEquals(-1, self.lastIndex());
		assertEquals(0, self.nextIndex());
		assertEquals(-1, self.previousIndex());
		// @ensures (\forall User u; hasSubscriptionTo(u); u.lastIndex() == -1 &&
		// u.nextIndex() == 0 && u.previousIndex() == -1);
		for (User u : self.getSubscriptions()) {
			assertEquals(-1, u.lastIndex());
			assertEquals(0, u.nextIndex());
			assertEquals(-1, u.previousIndex());
		}
		// @ensures \resmodel = new ListIterObserverAdapter(\result);
		ListIterObserver<Post> resmodel = new ListIterObserverAdapter<Post>(result);
		// @ensures !\resmodel.contains(null);
		assertFalse(resmodel.contains​(null));
		// @ensures \resmodel.containsAll(getPosts());
		assertTrue(resmodel.containsAll(self.getPosts()));
		// @ensures \resmodel.isSorted(Comparator.reverseOrder());		
		assertTrue(resmodel.isSorted(Comparator.reverseOrder()));
		// @ensures (\forall User u; hasSubscriptionTo(u); \resmodel.containsAll(getPosts()));
		for (User u : self.getSubscriptions()) {
			resmodel.containsAll(u.getPosts());
		}
		
		Post anyPost = new SimplePost("Some text");
		assertThrows(UnsupportedOperationException.class, () -> result.add(anyPost));
		if (self.getPostNb() > 0) {
			result.next();
			assertThrows(UnsupportedOperationException.class, () -> result.remove());
			assertThrows(UnsupportedOperationException.class, () -> result.set(anyPost));
		}

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method iterator
	 *
	 * Renvoie un Iterator sur les Post de cet utilisateur. Cet Iterator interdit
	 * toute modification et permet d'effectuer une itération indépendament de
	 * l'itérateur intégré. L'usage de l'itérateur renvoyé ne mofifie pas l'état de
	 * cette instance en tant qu'itérateur tel qu'il peut être observé à l'aide des
	 * méthodes de cette classe (notamment hasPrevious(), hasNext(), nextIndex(),
	 * ...).
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testiterator(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		ListIterator<Post> result = self.iterator();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \resmodel = new ListIterObserverAdapter(\result);
		ListIterObserver<Post> resmodel = new ListIterObserverAdapter<Post>(result);
		// @ensures \result != null && !\resmodel.contains(null);
		assertNotNull(result);
		assertFalse(resmodel.contains​(null));
		// @ensures \resmodel.toList().equals(getPosts());
		assertEquals(self.getPosts(), resmodel.toList());
		// @ensures \resmodel.isSorted(Comparator.reverseOrder());		
		assertTrue(resmodel.isSorted(Comparator.reverseOrder()));
		Post anyPost = new SimplePost("Some text");
		assertThrows(UnsupportedOperationException.class, () -> result.add(anyPost));
		if (self.getPostNb() > 0) {
			result.next();
			assertThrows(UnsupportedOperationException.class, () -> result.remove());
			assertThrows(UnsupportedOperationException.class, () -> result.set(anyPost));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method startIteration
	 *
	 * Initialise ce User pour le démarrage d'une nouvelle itération sur les Post de
	 * ce User. Cette itération s'effectue à partir du Post le plus récent, de sorte
	 * que chaque appel à next() renvoie un Post plus ancien.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void teststartIteration(User self) {
		assumeTrue(self != null);

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

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasNext
	 *
	 * Renvoie true si ce User possède un Post plus ancien pour l'itération en
	 * cours.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testhasNext(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasNext();

		// Post-conditions:
		// @ensures \result <==> nextIndex() < getPostNb();
		assertEquals(self.nextIndex() < self.getPostNb(), result);

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method next
	 *
	 * Renvoie le Post suivant (plus ancien) dans l'itération en cours et avance
	 * d'un élément dans l'itération.
	 * 
	 * @throws NoSuchElementException si hasNext() est false
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testnext(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		if (!self.hasNext()) {
			assertThrows(NoSuchElementException.class, () -> self.next());
			return;
		}

		// Oldies:
		// old in:@ensures \result.equals(\old(getNext()));
		Post oldNext = self.getNext();
		// old in:@ensures nextIndex() == \old(nextIndex()) + 1;
		// old in:@ensures previousIndex() == \old(nextIndex());
		// old in:@ensures lastIndex() == \old(nextIndex());
		int oldNextIndex = self.nextIndex();
		// old in:@ensures previousIndex() == \old(previousIndex()) + 1;
		int oldPreviousIndex = self.previousIndex();
		

		// Exécution:
		Post result = self.next();

		// Post-conditions:
		// @ensures \result.equals(\old(getNext()));
		assertEquals(oldNext, result);
		// @ensures nextIndex() == \old(nextIndex()) + 1;
		assertEquals(oldNextIndex + 1, self.nextIndex());
		// @ensures previousIndex() == \old(previousIndex()) + 1;
		assertEquals(oldPreviousIndex + 1, self.previousIndex());
		// @ensures previousIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.previousIndex());
		// @ensures lastIndex() == \old(nextIndex());
		assertEquals(oldNextIndex, self.lastIndex());
		// @ensures lastIndex() == previousIndex();
		assertEquals(self.previousIndex(), self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getNext
	 *
	 * Renvoie le Post suivant (plus ancien) dans l'itération en cours sans modifié
	 * l'état de ce User.
	 * 
	 * @throws NoSuchElementException si hasNext() est false
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetNext(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasNext();
		if (!self.hasNext()) {
			assertThrows(NoSuchElementException.class, () -> self.getNext());
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Post result = self.getNext();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(getPost(nextIndex()));
		assertEquals(self.getPost(self.nextIndex()), result);
		// @ensures hasPrevious() ==> getPrevious().isAfter(\result);
		if (self.hasPrevious()) {
			assertTrue(self.getPrevious().isAfter(result));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method hasPrevious
	 *
	 * Renvoie true si ce User possède un Post plus récent pour l'itération en
	 * cours.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testhasPrevious(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		boolean result = self.hasPrevious();

		// Post-conditions:
		// @ensures \result <==> previousIndex() >= 0;
		assertEquals(self.previousIndex() >= 0, result);
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
	 * Renvoie le Post précedent (plus récent) dans l'itération en cours et recule
	 * d'un élément dans l'itération.
	 * 
	 * @throws NoSuchElementException si hasPrevious() est false
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testprevious(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		if (!self.hasPrevious()) {
			assertThrows(NoSuchElementException.class, () -> self.previous());
			return;
		}

		// Oldies:
		// old in:@ensures \result.equals(\old(getPrevious()));
		Post oldPrevious = self.getPrevious();
		// old in:@ensures nextIndex() == \old(nextIndex()) - 1;
		int oldNextIndex = self.nextIndex();
		// old in:@ensures previousIndex() == \old(previousIndex()) - 1;
		// old in:@ensures nextIndex() == \old(previousIndex());
		// old in:@ensures lastIndex() == \old(previousIndex());
		int oldPreviousIndex = self.previousIndex();

		// Exécution:
		Post result = self.previous();

		// Post-conditions:
		// @ensures \result.equals(\old(getPrevious()));
		assertEquals(oldPrevious, result);
		// @ensures nextIndex() == \old(nextIndex()) - 1;
		assertEquals(oldNextIndex - 1, self.nextIndex());
		// @ensures previousIndex() == \old(previousIndex()) - 1;
		assertEquals(oldPreviousIndex - 1, self.previousIndex());
		// @ensures nextIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.nextIndex());
		// @ensures lastIndex() == \old(previousIndex());
		assertEquals(oldPreviousIndex, self.lastIndex());
		// @ensures lastIndex() == nextIndex();
		assertEquals(self.nextIndex(), self.lastIndex());

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method getPrevious
	 *
	 * Renvoie le Post précédent (plus récent) dans l'itération en cours sans
	 * modifié l'état de ce User.
	 * 
	 * @throws NoSuchElementException si hasPrevious() est false
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testgetPrevious(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:
		// @requires hasPrevious();
		if (!self.hasPrevious()) {
			assertThrows(NoSuchElementException.class, () -> self.getPrevious());
			return;
		}

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		Post result = self.getPrevious();

		// Post-conditions:
		// @ensures \result != null;
		assertNotNull(result);
		// @ensures \result.equals(getPost(previousIndex()));
		assertEquals(self.getPost(self.previousIndex()), result);
		// @ensures hasNext() ==> \result.isAfter(getNext());
		if (self.hasNext()) {
			assertTrue(result.isAfter(self.getNext()));
		}

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method nextIndex
	 *
	 * Renvoie l'index du Post qui sera renvoyé par le prochain appel à next(). Si
	 * l'itération est arrivée à la fin la valeur renvoyée est getPostNb().
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testnextIndex(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.nextIndex();

		// Post-conditions:
		// @ensures \result == getPostNb() <==> !hasNext();
		assertEquals(!self.hasNext(), result == self.getPostNb());
		// @ensures hasNext() <==> \result >= 0 && \result < getPostNb();
		assertEquals(self.hasNext(), result >= 0 && result < self.getPostNb());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method previousIndex
	 *
	 * Renvoie l'index du Post qui sera renvoyé par le prochain appel à previous().
	 * Si l'itération est arrivée au début la valeur renvoyée est -1.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testpreviousIndex(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.previousIndex();

		// Post-conditions:
		// @ensures \result == -1 <==> !hasPrevious();
		assertEquals(!self.hasPrevious(), result == -1);
		// @ensures hasPrevious() <==> \result >= 0 && \result < getPostNb();
		assertEquals(self.hasPrevious(), result >= 0 && result < self.getPostNb());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method lastIndex
	 *
	 * Renvoie l'index du Post renvoyé par le dernier appel à previous() ou next().
	 * Si previous() et next() n'ont pas été appelée depuis le dernier appel à
	 * StartIteration() (ou l'appel du constructeur), renvoie -1.
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testlastIndex(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		// Pré-conditions:

		// Save state for purity check:
		saveState(self);

		// Oldies:

		// Exécution:
		int result = self.lastIndex();

		// Post-conditions:
		// @ensures \result == nextIndex() || \result == previousIndex();
		assertTrue(result == self.nextIndex() || result == self.previousIndex());

		// Assert purity:
		assertPurity(self);

		// Invariant:
		assertInvariant(self);
	}

	/**
	 * Test method for method remove
	 *
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("userProvider")
	public void testremove(User self) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);
		
		assertThrows(UnsupportedOperationException.class, () -> self.remove());

	}

	/**
	 * Test method for method set
	 *
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("userAndPostProvider")
	public void testset(User self, Post e) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		assertThrows(UnsupportedOperationException.class, () -> self.set(e));

	}

	/**
	 * Test method for method add
	 *
	 * Opération non supportée.
	 * 
	 * @throws UnsupportedOperationException toujours
	 */
	@ParameterizedTest
	@MethodSource("userAndPostProvider")
	public void testadd(User self, Post e) {
		assumeTrue(self != null);

		// Invariant:
		assertInvariant(self);

		assertThrows(UnsupportedOperationException.class, () -> self.add(e));

		// Invariant:
		assertInvariant(self);
	}
} // End of the test class for User
