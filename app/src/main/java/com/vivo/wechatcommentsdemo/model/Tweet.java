package com.vivo.wechatcommentsdemo.model;

import java.util.List;

/**
 * Created by Administrator on 2017/11/8.
 */

public class Tweet {

    private String content;
    private List<Url> images;
    private Sender sender;
    private List<Comment> comments;

    public Tweet(String _content, List<Url> _images, Sender _sender, List<Comment> _comments) {
        content = _content;
        images = _images;
        sender = _sender;
        comments = _comments;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Url> getImages() {
        return images;
    }

    public void setImages(List<Url> images) {
        this.images = images;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("content[");
        sb.append(content + "],");
        sb.append("images{");
        sb.append(images + "},");
        sb.append("sender[");
        sb.append(sender + "],");
        sb.append("comments{");
        sb.append(comments + "}");
        return sb.toString();
    }

    public static class Sender {
        private String username;
        private String nick;
        private String avatar;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("username[");
            sb.append(username + "],");
            sb.append("nick[");
            sb.append(nick + "],");
            sb.append("avatar[");
            sb.append(avatar + "]");
            return sb.toString();
        }
    }

    public static class Comment {
        String content;
        Sender sender;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Sender getSender() {
            return sender;
        }

        public void setSender(Sender sender) {
            this.sender = sender;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("comment_content[");
            sb.append(content + "],");
            sb.append("sender[");
            sb.append(sender.toString() + "]");
            return sb.toString();
        }
    }

    public static class Url {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String toString() {
            return "url[" + url + "]";
        }
    }
}
