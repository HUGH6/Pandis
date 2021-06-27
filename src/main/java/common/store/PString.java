package common.store;

public class PString {
    private StringBuilder content;

    public PString() {
        this.content = new StringBuilder();
    }

    public PString(String content) {
        this.content = new StringBuilder(content);
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    };

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof PString) {
            return other.hashCode() == this.hashCode() ? true : false;
        } else {
            return false;
        }
    }

}
