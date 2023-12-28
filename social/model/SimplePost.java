package social.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

public class SimplePost implements Post {

    private String texte;
    private Instant creationDate;
    private Set<User> likedUsers;

    public SimplePost(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.texte = text;
        this.creationDate = Instant.now();
        this.likedUsers = new HashSet<User>();
    }

    @Override
    public Instant getDate() {
        return this.creationDate;
    }

    @Override
    public String getText() {
        return this.texte;
    }

    @Override
    public int getLikeNumber() {
        return this.likedUsers.size();
    }

    @Override
    public boolean hasLikeFrom(User u) {
        return this.likedUsers.contains(u);
    }

    @Override
    public boolean addLikeFrom(User u) {
        if (u == null) {
            throw new NullPointerException();
        }
        return this.likedUsers.add(u);
    }

    @Override
    public Set<User> getLikers() {
        return Collections.unmodifiableSet(this.likedUsers);
    }

    @Override
    public ListIterator<User> iterator() {
        return Collections.unmodifiableList(new ArrayList<User>(this.likedUsers)).listIterator();
    }

    @Override
    public int compareTo(Post p) {
        if (p == null) {
            throw new NullPointerException();
        }
        return this.getDate().compareTo(p.getDate());
    }

    @Override
    public boolean isAfter(Post p) {
        if (p == null) {
            throw new NullPointerException();
        }
        return this.getDate().isAfter(p.getDate());
    }

    @Override
    public boolean isBefore(Post p) {
        if (p == null) {
            throw new NullPointerException();
        }
        return this.getDate().isBefore(p.getDate());
    }
}
