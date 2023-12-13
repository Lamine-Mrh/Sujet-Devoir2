package social.model;

public class RePost extends SimplePost{

    private User author;
    private Post post;
    
    public RePost(String text, User subPostAuthor, Post subPost){
        super(text);
        this.author = subPostAuthor;
        this.post = subPost;
    }

    @Override
    public String getText(){
        return "Auteur: " + author.getFollowerNb() + "\nTexte: " + post.getText() + "\nContenue: " + super.getText();
    }

}
