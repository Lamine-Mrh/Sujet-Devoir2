/**
 * 
 */
package social.model.test;

import java.util.ListIterator;

/**
 * Ajout de la méthode get(int) avec une spécification identique à celle de
 * l'interface List.
 * 
 * @author Marc Champesme
 * @since 22/08/2023
 * @version 22/08/2023
 * 
 */
public interface ListIterObserver<E> extends IterObserver<E>, ListIterator<E> {
	/**
	 * Returns the element at the specified position in this iterator.
	 * 
	 * @param i index of the element to return
	 * @return the element at the specified position in this iterator
	 * 
	 * @throws IndexOutOfBoundsException if the index is out of range (index < 0 ||
	 *                                   index >= size())
	 * 
	 * @pure
	 */
	public E get(int i);
}
