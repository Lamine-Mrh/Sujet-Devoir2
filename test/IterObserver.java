package test;

/**
 * 
 */

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Les méthodes définnies dans cette interface permettent d'observer la
 * collection d'éléments énumérables par un itérateur fini, comme si ces
 * éléments
 * appartenaient à une Collection. Ces méthodes sont uniquement destinées à
 * permettre d'effectuer des tests afin de vérifier que la collection d'éléments
 * énumérés par un itérateur correspond à ce qui est voulu.
 * 
 * Ces méthodes sont "pures" dans le sens où leur exécution ne modifie pas
 * l'état de l'itérateur observé. Par conséquent, si l'implémentation d'une de
 * ces méthodes modifie l'état de l'itérateur observé, elle doit garantir un
 * rétablissement de l'état initial à la fin de son exécution.
 * 
 * Les méthodes spécifiées dans cette interface sont les méthodes de l'interface
 * Collection qui n'entrainent aucune modification de la Collection (i.e. les
 * méthodes utilisables sur une "Unmodifiable Collection"), à l'exception de la
 * méthode iterator(). Les spécifications de ces méthodes sont identiques à
 * celles de l'interface Collection. Deux méthodes (toList et toSet) ne figurant
 * pas dans l'interface Collection ont néanmoins été ajoutées.
 * 
 * @author Marc Champesme
 * @since 22/08/2023
 * @version 22/08/2023
 */
public interface IterObserver<E> extends Iterator<E> {
	/**
	 * Returns true if this collection contains the specified element. More
	 * formally, returns true if and only if this collection contains at least one
	 * element e such that Objects.equals(o, e).
	 * 
	 * @param o element whose presence in this collection is to be tested
	 * @return true if this collection contains the specified element
	 * 
	 * @pure
	 */
	public boolean contains​(Object o);

	/**
	 * Returns true if this collection contains all of the elements in the specified
	 * collection.
	 * 
	 * @param c collection to be checked for containment in this collection
	 * @return true if this collection contains all of the elements in the specified
	 *         collection
	 * 
	 * @throws NullPointerException if the specified collection is null.
	 * 
	 * @pure
	 */
	public boolean containsAll(Collection<?> c);

	/**
	 * Returns the number of elements in this collection. If this collection
	 * contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 * 
	 * @return the number of elements in this collection
	 * 
	 * @pure
	 */
	public int size();

	/**
	 * Returns true if this collection contains no elements.
	 * 
	 * @return true if this collection contains no elements
	 * 
	 * @pure
	 */
	public boolean isEmpty();

	/**
	 * Returns an array containing all of the elements in this iterator. If this
	 * collection makes any guarantees as to what order its elements are returned by
	 * its iterator, this method must return the elements in the same order. The
	 * returned array's runtime component type is Object.
	 * 
	 * The returned array will be "safe" in that no references to it are maintained
	 * by this iterator. (In other words, this method must allocate a new array even
	 * if this iterator is backed by an array). The caller is thus free to modify
	 * the returned array.
	 * 
	 * 
	 * @return an array, whose runtime component type is Object, containing all of
	 *         the elements in this collection
	 * 
	 * @pure
	 */
	public Object[] toArray();

	/**
	 * Returns an array containing all of the elements in this collection; the
	 * runtime type of the returned array is that of the specified array. If the
	 * collection fits in the specified array, it is returned therein. Otherwise, a
	 * new array is allocated with the runtime type of the specified array and the
	 * size of this collection.
	 * 
	 * If this collection fits in the specified array with room to spare (i.e., the
	 * array has more elements than this collection), the element in the array
	 * immediately following the end of the collection is set to null. (This is
	 * useful in determining the length of this collection only if the caller knows
	 * that this collection does not contain any null elements.)
	 * 
	 * @param <T> the component type of the array to contain the collection
	 * @param a   the array into which the elements of this collection are to be
	 *            stored, if it is big enough; otherwise, a new array of the same
	 *            runtime type is allocated for this purpose.
	 * @return an array containing all of the elements in this collection
	 * 
	 * @throws ArrayStoreException  if the runtime type of any element in this
	 *                              collection is not assignable to the runtime
	 *                              component type of the specified array
	 * @throws NullPointerException if the specified array is null
	 * 
	 * @pure
	 */
	public <T> T[] toArray​(T[] a);

	/**
	 * Returns a List containing all of the elements in this iterator in proper
	 * sequence (from first to last element).
	 * 
	 * The returned List will be "safe" in that no references to it are maintained
	 * by this iterator. (In other words, this method must allocate a new List even
	 * if this iterator is backed by a List). The caller is thus free to modify the
	 * returned List.
	 * 
	 * @return a List containing all of the elements in this iterator in proper
	 *         sequence
	 * 
	 * @pure
	 */
	public List<E> toList();

	/**
	 * Returns a Set containing all of the elements in this iterator.
	 * 
	 * The returned Set will be "safe" in that no references to it are maintained by
	 * this iterator. (In other words, this method must allocate a new Set even if
	 * this iterator is backed by a Set). The caller is thus free to modify the
	 * returned Set.
	 * 
	 * @return a Set containing all of the elements in this iterator
	 * 
	 * @pure
	 */
	public Set<E> toSet();

	/**
	 * Teste si les éléments de cet Itérateur sont triés par ordre croissant.
	 * 
	 * @param cmp le comparateur à utiliser pour ce test
	 * 
	 * @return true si les éléments de cet Itérateur sont triés par ordre croissant;
	 *         false sinon
	 * 
	 * @throws NullPointerException si le comparateur spécifié est null ou si cet
	 *                              itérateur contient null
	 * 
	 * @requires cmp != null;
	 * @requires (\forall E obj; contains(obj); obj != null);
	 * @ensures \result <==> (\forall int i,j; i >= 0 && i < j && j < size();
	 *          cmp.compare(toList().get(i), toList().get(j)) <= 0);
	 * 
	 * @pure
	 */
	public boolean isSorted(Comparator<? super E> cmp);
}
