package Main.githubwebhooks;

class Issue { // thanks Austin again! :D

    /**
     * The action that was performed. Can be one of "assigned", "unassigned", "labeled", "unlabeled", "opened",
     * "edited", "closed", or "reopened".
     */
    String action;

    /**
     * The actual issue.
     */
    IssueContent issue;

    /**
     * The repository the issues were submitted to.
     */
    RepositoryContent repository;

    SenderContent sender;

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

    class IssueContent {

        /**
         * The issue number.
         */
        int number;

        /**
         * The issue url.
         */
        String html_url;

        /**
         * The state (open or closed).
         */
        String state;

        /**
         * The issue title.
         */
        String title;

        /**
         * The issue body.
         */
        String body;

        /**
         * The user who created this issue.
         */
        IssueContent.UserContent user;

        class UserContent {

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

    class SenderContent {

        /**
         * The name of the sender.
         */
        String login;

    }
}
