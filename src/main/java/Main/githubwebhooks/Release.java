package Main.githubwebhooks;

class Release { // last one

    /**
     * The action that was performed. Currently, can only be "published".
     */
    public String action;

    /**
     * The actual release object.
     */
    ReleaseContent release;

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
        public String url;
    }

    class ReleaseContent {

        /**
         * The link to the release.
         */
        String html_url;

        /**
         * The tag name.
         */
        String tag_name;

        /**
         * The release name.
         */
        String name;

        /**
         * The release description.
         */
        String body;

        /**
         * Whether this is a draft.
         */
        boolean draft;

        /**
         * Whether this is a pre-release.
         */
        boolean prerelease;

        /**
         * The author of the release.
         */
        ReleaseContent.AuthorContent author;

        class AuthorContent {

            /**
             * The login name.
             */
            String login;

            /**
             * The link to the user.
             */
            String html_url;
        }
    }
}
