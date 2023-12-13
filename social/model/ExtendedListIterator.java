/**
 * 
 */
package social.model;

import java.util.ListIterator;

/**
 * Ajout de 4 méthodes à ListIterator pour en simplifier l'usage.
 * 
 * @param <E> le type des éléments énumérés
 * 
 * @invariant nextIndex() == previousIndex() + 1;
 * @invariant lastIndex() == nextIndex() || lastIndex() == previousIndex();
 * @invariant previousIndex() >= -1;
 * @invariant nextIndex() >= 0;
 * @invariant lastIndex() >= -1;
 * @invariant !hasPrevious() <==> previousIndex() == -1;
 * 
 * @author Marc Champesme
 * @since 01/12/2023
 * @version 04/12/2023
 */
public interface ExtendedListIterator<E> extends ListIterator<E> {
	/**
	 * (Re)Initialise ce ListIterateur pour le démarrage d'une nouvelle itération
	 * sur ses éléments.
	 * 
	 * @ensures !hasPrevious();
	 * @ensures previousIndex() == -1;
	 * @ensures nextIndex() == 0;
	 * @ensures lastIndex() == -1;
	 */
	void startIteration();

	/**
	 * Renvoie l'élément suivant (l'élément qui serait retourné par un appel à
	 * next()) dans l'itération en cours sans modifié l'état de ce ListIterator.
	 * 
	 * @return l'élément suivant dans l'itération en cours
	 * 
	 * @throws NoSuchElementException si hasNext() est false
	 * 
	 * @requires hasNext();
	 * @ensures previousIndex() == \old(previousIndex());
	 * @ensures nextIndex() == \old(nextIndex());
	 * @ensures lastIndex() == \old(lastIndex());
	 * 
	 * @pure
	 */
	E getNext();

	/**
	 * Renvoie l'élément précédent (l'élément qui serait retourné par un appel à
	 * previous()) dans l'itération en cours sans modifié l'état de ce ListIterator.
	 * 
	 * @return l'élément précédent dans l'itération en cours
	 * 
	 * @throws NoSuchElementException si hasPrevious() est false
	 * 
	 * @requires hasPrevious();
	 * @ensures previousIndex() == \old(previousIndex());
	 * @ensures nextIndex() == \old(nextIndex());
	 * @ensures lastIndex() == \old(lastIndex());
	 * 
	 * @pure
	 */
	E getPrevious();

	/**
	 * Renvoie l'index du dernier élément renvoyé par un appel à next() ou
	 * previous(). Renvoie -1 si l'itération n'a pas encore commencé (si aucun appel
	 * à next() ou previous() n'a encore été effectué).
	 * 
	 * @return l'index du dernier élément renvoyé par un appel à next() ou
	 *         previous()
	 * 
	 * @ensures \result == -1 ==> !hasPrevious();
	 * 
	 * @pure
	 */
	int lastIndex();

	/**
	 * Renvoie l'élément suivant dans l'itération en cours et avance d'un élément
	 * dans l'itération.
	 * 
	 * @return l'élément suivant dans l'itération en cours
	 * 
	 * @throws NoSuchElementException si hasNext() est false
	 * 
	 * @requires hasNext();
	 * @ensures \result.equals(\old(getNext()));
	 * @ensures nextIndex() == \old(nextIndex()) + 1;
	 * @ensures previousIndex() == \old(previousIndex()) + 1;
	 * @ensures previousIndex() == \old(nextIndex());
	 * @ensures lastIndex() == \old(nextIndex());
	 * @ensures lastIndex() == previousIndex();
	 */
	@Override
	E next();

	/**
	 * Renvoie l'élément précédent dans l'itération en cours et recule d'un élément
	 * dans l'itération.
	 * 
	 * @return l'élément précédent dans l'itération en cours
	 * 
	 * @throws NoSuchElementException si hasPrevious() est false
	 * 
	 * @requires hasPrevious();
	 * @ensures \result.equals(\old(getPrevious()));
	 * @ensures nextIndex() == \old(nextIndex()) - 1;
	 * @ensures previousIndex() == \old(previousIndex()) - 1;
	 * @ensures nextIndex() == \old(previousIndex());
	 * @ensures lastIndex() == \old(previousIndex());
	 * @ensures lastIndex() == nextIndex();
	 */
	@Override
	E previous();

}
