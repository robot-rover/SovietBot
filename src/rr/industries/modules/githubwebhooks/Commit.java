package rr.industries.modules.githubwebhooks;

class Commit { // thanks Austin! :D

    /**
     * The branch info in the form "refs/heads/{BRANCH}"
     */
    String ref;

    /**
     * The commits pushed.
     */
    CommitContent[] commits;

    /**
     * The repository the commits were pushed to.
     */
    RepositoryContent repository;

    class RepositoryContent {

        /**
         * The repo's name.
         */
        String full_name;

        /**
         * The repo's url.
         */
        String url;
    }

    class CommitContent {

        /**
         * The unique id for the commit.
         */
        String id;

        /**
         * The url for the commit.
         */
        String url;

        /**
         * Commit message.
         */
        String message;

        /**
         * The author of the commit.
         */
        CommitContent.AuthorContent author;

        class AuthorContent {

            /**
             * The login name.
             */
            String username;
        }
    }
}
