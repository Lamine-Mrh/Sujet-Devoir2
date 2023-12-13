package social.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class SimplePost implements Post{

    private String texte;
    private Instant creationDate;
    private Set<User> likedUsers;


    public SimplePost(String texte){
        this.texte = texte;
        this.creationDate = Instant.now();
        this.likedUsers = new HashSet<User>();
    }

    @Override
    public Instant getDate(){
        return this.creationDate;
    }
    
    @Override
    public String getText(){
        return this.texte;
    }

    @Override
    public int getLikeNumber(){
        return this.likedUsers.size();
    }

    @Override
    public boolean hasLikeFrom(User u){
        return this.likedUsers.contains(u);
    }

    @Override
    public boolean addLikeFrom(User u){
        try {
            return likedUsers.add(u);
        } catch (NullPointerException e) {
            throw new InternalError();
        }
    }

    @Override
    public Set<User> getLikers(){
        return Set.copyOf(likedUsers);
    }

    @Override
    public ListIterator<User> iterator(){
        return List.copyOf(likedUsers).listIterator();
    }

    @Override
    public int compareTo(Post p){
        try {
            return this.getDate().compareTo(p.getDate());
        } catch (NullPointerException e) {
            throw new InternalError();
        }
        
    }

    @Override
    public boolean isAfter(Post p){
        try {
            return this.getDate().isAfter(p.getDate());            
        } catch (NullPointerException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean isBefore(Post p){
        try {
            return this.getDate().isBefore(p.getDate());            
        } catch (NullPointerException e) {
            throw new InternalError();
        }
    }
}
