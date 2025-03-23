package mfy.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import mfy.server.domain.message.entity.SystemMessage;
import mfy.server.domain.message.repository.SystemMessageRepository;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.entity.type.Category;
import mfy.server.domain.project.entity.type.Language;
import mfy.server.domain.project.repository.ProjectRepository;
import mfy.server.domain.user.entity.User;
import mfy.server.domain.user.entity.type.Role;
import mfy.server.domain.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final SystemMessageRepository systemMessageRepository;

        @Override
        public void run(String... args) throws Exception {

                long totalAccounts = userRepository.count();
                if (totalAccounts > 0) {
                        return;
                }
                User user = new User("test", "test@email.com", "Aa123456&");
                user.updateIsVerified();
                User user2 = new User("test2", "test2@email.com", "Aa123456&");
                user2.updateIsVerified();
                User user3 = new User("test3", "test3@email.com", "Aa123456&");
                user3.updateIsVerified();
                User admin = new User("admin", "admin@email.com", "Aa123456&");
                admin.updateIsVerified();
                admin.updateRole(Role.ADMIN);
                userRepository.saveAll(Arrays.asList(
                                admin,
                                user,
                                user2,
                                user3));

                Project project = new Project("Build your career as a backend developer", "src1", Language.ENG,
                                Language.VIE,
                                user, Category.OTHER, "Backend development is a work of art");
                project.addTranslators(user2, user3, admin);
                Project project2 = new Project("The negative impact of the AI outbreak on developers", "src2",
                                Language.ENG,
                                Language.VIE, user2, Category.OTHER,
                                "Abuse of AI chatboxes in coding undermines developers");
                project2.addTranslators(user, user3, admin);
                Project project3 = new Project("Become a competent developer", "src3", Language.ENG, Language.VIE,
                                user3,
                                Category.OTHER, "The future of software development");
                project3.addTranslators(user, user2, admin);

                projectRepository.saveAll(Arrays.asList(
                                project,
                                project2,
                                project3));

                SystemMessage welcome = new SystemMessage("👋 Welcome to Miracle4You!", true);
                SystemMessage introduction = new SystemMessage(
                                "🎉 Miracle4You is a collaborative platform to make subtitles for meaningful digital content.",
                                true);
                SystemMessage about = new SystemMessage(
                                "🔧 This project is under development, for more information visit https://github.com/NDinhChien/miracle4you",
                                true);
                SystemMessage newFeature = new SystemMessage(
                                "📢 New feature alert: You can now attach files to your messages! Try it out in your next conversation.",
                                true);
                systemMessageRepository.saveAll(Arrays.asList(
                                welcome,
                                introduction,
                                about,
                                newFeature));
        }
}