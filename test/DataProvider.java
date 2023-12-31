package test;

/**
 *
 */

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import social.model.Post;
import social.model.RePost;
import social.model.SimplePost;
import social.model.User;
import social.model.FusionSortedIterator;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 */
public class DataProvider {
	private static Random randGen = new Random();
	static final int LG_STREAM = 500;
	private static List<String> goodUserNames = Arrays.asList("Marcel", "Adam", "Sonia", "Idir", "Mohamed", "Marc",
			"Ali", "Ziad", "Lyes", "Ayman", "Mounir", "Pierre", "Chanez", "Lamia", "Yanis", "Faycal", "Boris", "Imam",
			"Naila", "Zahra", "Rosa", "Lisa", "Sheraz", "Nima", "Aliou", "Issa", "Mamadou", "Ismael");
	private static List<String> otherGoodNames = Arrays.asList("Pomme", "Poire", "Banane", "Abricot", "Carotte",
			"Courgette", "Patate", "Orange", "Citron", "Aubergine", "Oignon", "Haricot", "Igname", "Potiron", "Poivron",
			"Piment", "Navet", "Ail", "Persil", "Coriandre", "Poivre", "Sel", "Curcuma");
	private static List<String> badNames = Arrays.asList("", " ", "  ", "\n \t", null);
	private static List<User> allUsers;
	private static List<Post> allPosts;
	private static int testPostCounter = 0;

	static {
		initUsers(goodUserNames);
	}

	private static void initUsers(List<String> userNames) {
		allUsers = new ArrayList<User>(userNames.size());
		for (String name : userNames) {
			allUsers.add(new User(name, "pass" + name));
		}
		addPostsToUsers(allUsers);
		addLikesToPosts(allPosts);
		addSubscriptionsToUsers(allUsers);
	}

	private static void addPostsToUsers(List<User> userList) {
		int nbMsg = 1000;
		allPosts = new ArrayList<Post>(nbMsg);
		while (nbMsg > 0) {
			User u = getRandomElt(userList);
			Post p = new SimplePost("Message n°" + nbMsg + " from " + u.getName());
			allPosts.add(u.addPost(p));
			nbMsg--;
		}
	}

	private static void addSubscriptionsToUsers(List<User> userList) {
		int nbSubscription = 100;
		while (nbSubscription > 0) {
			User u = getRandomElt(userList);
			User subscription = getRandomElt(userList);
			if (u != subscription) {
				u.addSubscriptionTo(subscription);
				nbSubscription--;
			}
		}
	}

	private static void addLikesToPosts(List<Post> postList) {
		int likeNb = 1000;
		while (likeNb > 0) {
			getRandomElt(postList).addLikeFrom(getRandomElt(allUsers));
			likeNb--;
		}
	}

	public static Post postSupplier() {
		if (randBool(50)) {
			return null;
		}
		if (randBool()) {
			User u = userSupplier();
			if (u.getPostNb() > 1) {
				Post p = getRandomElt(u.getPosts());
				if (randBool()) {
					return p;
				}
				return new RePost("Test RePost " + testPostCounter++, u, p);
			}
		}
		return new SimplePost("New test SimplePost" + testPostCounter++);
	}

	public static User userSupplier() {
		User u = getRandomElt(User.getAllUser());
		if (randBool() && u.getPostNb() > 2 && u.lastIndex() == -1) {
			int count = randInt(u.getPostNb());
			while (u.hasNext() && count > 0) {
				u.next();
				count--;
			}
		}
		return u;
	}

	public static String stringSupplier() {
		if (randBool(50)) {
			return getRandomElt(badNames);
		}
		if (randBool()) {
			return getRandomElt(goodUserNames);
		}
		return getRandomElt(otherGoodNames);
	}

	public static Set<User> setOfListIterSupplier() {
		return randomSet(10, () -> userSupplier());
	}

	// public static DateSortedIterator dateSortedIterSupplier() {
	// return new DateSortedIterator(listOfListIterSupplier());
	// }

	public static FusionSortedIterator<Post, User> fusSortedIterSupplier() {
		return new FusionSortedIterator<Post, User>(setOfListIterSupplier(), Comparator.reverseOrder());

	}

	// int and boolean supplier helpers:
	/**
	 * Renvoie un int obtenue par un générateur pseudo-aléatoire.
	 *
	 * @param max la valeur maximale du nombre aléatoire attendu
	 *
	 * @return un entier >= 0 et < max
	 *
	 * @throws IllegalArgumentException si max <= 0
	 *
	 * @requires max > 0;
	 * @ensures \result >= 0;
	 * @ensures \result < max;
	 */
	public static int randInt(int max) {
		return randGen.nextInt(max);
	}

	/**
	 * Renvoie un int obtenue par un générateur pseudo-aléatoire.
	 *
	 * @param min la valeur minimale du nombre aléatoire attendu
	 * @param max la valeur maximale du nombre aléatoire attendu
	 *
	 * @return un entier >= min et < max
	 *
	 * @throws IllegalArgumentException si max <= min
	 *
	 * @requires max > min;
	 * @ensures \result >= min;
	 * @ensures \result < max;
	 */
	public static int randInt(int min, int max) {
		return randInt(max - min) + min;
	}

	/**
	 * Renvoie un tableau d'entier dont la longueur maximale est spécifiée et dont
	 * les éléments sont tirés aléatoirement entre les bornes spécifiées.
	 * 
	 * @param maxLength longueur maximale du tableau
	 * @param min       borne inférieure (inclusive) des valeurs du tableau
	 * @param max       borne supérieure (exclusive) des valeurs du tableau
	 * 
	 * @return un tableau d'entier deont la longueur maximale est spécifiée et dont
	 *         les éléments sont tirés aléatoirement entre les bornes spécifiées
	 * 
	 * @throws IllegalArgumentException si max <= min ou maxLength < 0;
	 * 
	 * @requires maxLength >= 0;
	 * @requires max > min;
	 * @ensures \result != null;
	 * @ensures \result.length <= maxLength;
	 * @ensures (\forall int i; i >= 0 && i < \result.length; \result[i] >= min &&
	 *          \result[i] < max);
	 */
	public static int[] randTabInt(int maxLength, int min, int max) {
		int realLength = randInt(maxLength + 1);
		return IntStream.generate(() -> randInt(min, max)).limit(realLength).toArray();
	}

	/**
	 * Renvoie une valeur booléenne obtenue par un générateur pseudo-aléatoire.
	 *
	 * @return une valeur booléenne aléatoire
	 */
	public static boolean randBool() {
		return randGen.nextBoolean();
	}

	/**
	 * Renvoie une valeur booléenne obtenue par un générateur pseudo-aléatoire. La
	 * valeur renvoyée a une probabilité d'être true similaire à la probabilité que
	 * randInt(max) renvoie la valeur 0.
	 *
	 * @return une valeur booléenne aléatoire
	 * 
	 * @throws IllegalArgumentException si max <= 0
	 * 
	 * @requires max > 0;
	 */
	public static boolean randBool(int max) {
		return randGen.nextInt(max) == 0;
	}

	/**
	 * Renvoie un élément tiré aléatoirement parmi les éléments de la collection
	 * spécifiée.
	 *
	 * @requires c != null;
	 * @requires !c.isEmpty();
	 * @ensures c.contains(\result);
	 *
	 * @param <T> Type des éléments de la collection spécifiée
	 * @param c   collection dans laquelle est choisi l'élément retourné
	 *
	 * @return un élément tiré aléatoirement parmi les éléments de la collection
	 *         spécifiée
	 * 
	 * @throws NullPointerException     si l'argument spécifié est null
	 * @throws IllegalArgumentException si l'argument spécifié est vide
	 */
	public static <T> T getRandomElt(Collection<T> c) {
		int index = randInt(c.size());
		if (c instanceof List<?>) {
			return ((List<T>) c).get(index);
		}
		int i = 0;
		for (T elt : c) {
			if (i == index) {
				return elt;
			}
			i++;
		}
		throw new NoSuchElementException(); // Ne peut pas arriver
	}

	/**
	 * Renvoie un Set dont la taille maximale est l'entier spécifié et dont les
	 * éléments sont obtenus à l'aide du Supplier spécifié.
	 * 
	 * @param <T>         le type des éléments du Set
	 * 
	 * @param maxLength   taille maximale du Set généré
	 * @param eltSupplier le Supplier utilisé pour générer les éléments du Set
	 * @return un Set dont la taille maximale est l'entier spécifié et dont les
	 *         éléments sont obtenus à l'aide du Supplier spécifié
	 * 
	 * @throws NullPointerException     si le Supplier spécifié est null
	 * @throws IllegalArgumentException si maxLength < 0
	 * 
	 * @requires maxLength >= 0;
	 * @requires eltSupplier != null;
	 * @ensures \result != null;
	 * @ensures \result.size() <= maxLength;
	 */
	public static <T> Set<T> randomSet(int maxLength, Supplier<? extends T> eltSupplier) {
		int realLength = randInt(maxLength + 1);
		return Stream.generate(eltSupplier).limit(realLength).collect(Collectors.toCollection(HashSet<T>::new));
	}

	public static <T> void assertIsUnmodifiable(Collection<T> c, Supplier<? extends T> s) {
		T anyElt = s.get();
		List<T> anyList = Collections.singletonList(anyElt);
		assertThrows(UnsupportedOperationException.class, () -> c.add(anyElt));
		assertThrows(UnsupportedOperationException.class, () -> c.addAll(anyList));
		assertThrows(UnsupportedOperationException.class, () -> c.clear());

		assertThrows(UnsupportedOperationException.class, () -> c.remove(anyElt));
		assertThrows(UnsupportedOperationException.class, () -> c.removeAll(anyList));
		assertThrows(UnsupportedOperationException.class, () -> c.removeIf((e) -> true));
		assertThrows(UnsupportedOperationException.class, () -> c.retainAll(anyList));
		assertIsUnmodifiable(c.iterator());
		if (c instanceof List<?>) {
			List<T> l = (List<T>) c;
			assertThrows(UnsupportedOperationException.class, () -> l.add(0, anyElt));
			assertThrows(UnsupportedOperationException.class, () -> l.addAll(0, anyList));
			assertThrows(UnsupportedOperationException.class, () -> l.replaceAll((e) -> anyElt));
			assertIsUnmodifiable(l.listIterator());
			assertIsUnmodifiable(l.listIterator(0));
			if (!l.isEmpty()) {
				assertThrows(UnsupportedOperationException.class, () -> l.remove(0));
				assertThrows(UnsupportedOperationException.class, () -> l.set(0, anyElt));
			}
		}
	}

	public static <T> void assertIsUnmodifiable(Iterator<T> iter) {
		if (iter.hasNext()) {
			T elt = iter.next();
			assertThrows(UnsupportedOperationException.class, () -> iter.remove());
			if (iter instanceof ListIterator<?>) {
				ListIterator<T> listIter = (ListIterator<T>) iter;
				assertThrows(UnsupportedOperationException.class, () -> listIter.set(elt));
			}
		}
		if (iter instanceof ListIterator<?>) {
			ListIterator<T> listIter = (ListIterator<T>) iter;
			assertThrows(UnsupportedOperationException.class, () -> listIter.add(null));
		}
	}
}
