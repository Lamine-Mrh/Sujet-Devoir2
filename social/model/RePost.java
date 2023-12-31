
public class RePost extends SimplePost {

    private User author;
    private Post post;

    public RePost(String text, User subPostAuthor, Post subPost) {
        super(text);
        if (subPostAuthor == null || subPost == null) {
            throw new NullPointerException("subPostAuthor or subPost cannot be null");
        }
        this.author = subPostAuthor;
        this.post = subPost;
    }

    @Override
    public String getText() {
        return super.getText() + "\n" + "RePost from " + this.author.getName() + " : " + this.post.getText();
    }
}