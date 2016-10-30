package sample_java;

import io.reactivex.functions.Consumer;
import io.rx_cache2.Reply;
import java.io.File;
import java.util.List;
import sample_data.Repository;
import sample_data.entities.User;

public class App {
    private int idLastUserQueried = 1;
    private boolean close, isInMainMenu, isInUsers, isInProfile;
    private final Repository repository;
    private final static int USERS = 0, PROFILE = 1, CLOSE = 2,
            SHOW_NEXT_PAGE = 0, RETURN_FIRST_PAGE = 1, RETURN_FIRST_PAGE_AND_CLEAR_CACHE_PAGE = 2, BACK = 3,
            SHOW_CURRENT_USER = 0, LOG_IN_USER_RANDOM_USER = 1, LOG_OUT_USER = 2;

    public App() {
        File cacheDir = new File(System.getProperty("user.home"), "Desktop");
        repository = new Repository(cacheDir);
        isInMainMenu = true;
    }

    public void printInstructionsForAvailableOptions() {
        if (isInMainMenu) {
            System.out.println("#0 Users - #1 Profile - #2 Close");
        } else if(isInUsers) {
            System.out.println("#0 Show Next Page - #1 Return first page - #2 Return and clear page - #3 Back");
        } else if(isInProfile) {
            System.out.println("#0 Show Current User - #1 Login random user - #2 Logout user - #3 Back");
        }
    }

    public void processNewInput(int input) {
        if (isInMainMenu) {
            if (input == CLOSE) close = true;
            else if(input == USERS) {
                isInMainMenu = false;
                isInUsers = true;
                isInProfile = false;
            } else if(input == PROFILE) {
                isInMainMenu = false;
                isInUsers = false;
                isInProfile = true;
            }
        } else if(isInUsers) {
            if (input == BACK) isInMainMenu = true;
            else if(input == SHOW_NEXT_PAGE) printUsers(false);
            else if(input == RETURN_FIRST_PAGE) {
                idLastUserQueried = 1;
                printUsers(false);
            } else if(input == RETURN_FIRST_PAGE_AND_CLEAR_CACHE_PAGE) {
                idLastUserQueried = 1;
                printUsers(true);
            }
        } else if(isInProfile) {
            if (input == BACK) isInMainMenu = true;
            else if(input == SHOW_CURRENT_USER) showCurrentUser();
            else if(input == LOG_IN_USER_RANDOM_USER) loginRandomUser();
            else if(input == LOG_OUT_USER) logoutUser();
        }
    }

    public boolean close() {
        return close;
    }

    private void printUsers(boolean update) {
        repository.getUsers(idLastUserQueried, update).subscribe(new Consumer<Reply<List<User>>>() {
            @Override public void accept(Reply<List<User>> reply) throws Exception {
                System.out.println("Source: " + reply.getSource().name());

                for (User user : reply.getData()) {
                    System.out.println(user);
                }

                idLastUserQueried = reply.getData().get(reply.getData().size()-1).getId();
            }
        });
    }

    private void showCurrentUser() {
        repository.getLoggedUser(false).subscribe(new Consumer<Reply<User>>() {
            @Override public void accept(Reply<User> userReply) throws Exception {
                System.out.println("Current user");
                System.out.println(userReply.getData());
            }
        }, new Consumer<Throwable>() {
            @Override public void accept(Throwable e) throws Exception {
                System.out.println(e.getCause());
            }
        });
    }

    private void loginRandomUser() {
        User user = new User(1, "Random", "Random Avatar");
        repository.loginUser(user.getLogin()).subscribe(new Consumer<Reply<User>>() {
            @Override public void accept(Reply<User> userReply) throws Exception {
                System.out.println("User logged");
            }
        });
    }

    private void logoutUser() {
        repository.logoutUser().subscribe(new Consumer<String>() {
            @Override public void accept(String message) throws Exception {
                System.out.println(message);
            }
        }, new Consumer<Throwable>() {
            @Override public void accept(Throwable e) throws Exception {
                System.out.println(e.getMessage());
            }
        });
    }
}
