package rr.industries.modules.githubwebhooks;

class PullRequest { // and again

    /**
     * The action that was performed. Can be one of "assigned", "unassigned", "labeled", "unlabeled", "opened",
     * "edited", "closed", or "reopened", or "synchronize". If the action is "closed" and the merged key is false,
     * the pull request was closed with unmerged commits. If the action is "closed" and the merged key is true, the
     * pull request was merged.
     */
    String action;
    /**
     * The pull request number.
     */
    int number;
    /**
     * The pull request object.
     */
    PullRequestContent pull_request;
    /**
     * The repository the pull request was submitted to.
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
        String url;
    }

    class SenderContent {

        /**
         * The name of the sender.
         */
        String login;

    }

    class PullRequestContent {

        /**
         * The issue number.
         */
        public int number;

        /**
         * The PR url.
         */
        String html_url;

        /**
         * Whether it was merged.
         */
        boolean merged;

        /**
         * The issue title.
         */
        String title;

        /**
         * The issue body.
         */
        String body;

        /**
         * Additions to the repo.
         */
        int additions;

        /**
         * Deletions from the repo.
         */
        int deletions;

        /**
         * The user who created this pull request.
         */
        PullRequestContent.UserContent user;

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
}
