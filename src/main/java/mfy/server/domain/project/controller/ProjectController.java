package mfy.server.domain.project.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfy.server.domain.project.dto.ProjectRequestDto.JoinProjectRequestDto;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectCreateRequestDto;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectSearchRequestDto;
import mfy.server.domain.project.dto.ProjectRequestDto.ProjectUpdateRequestDto;
import mfy.server.domain.project.dto.ProjectResponseDto.IProjectPublicDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectBasicDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectDto;
import mfy.server.domain.project.dto.ProjectResponseDto.ProjectPublicDto;
import mfy.server.domain.project.entity.Project;
import mfy.server.domain.project.service.ProjectService;
import mfy.server.global.dto.BaseResponse;
import mfy.server.global.security.UserDetailsImpl;
import mfy.server.global.service.ValidatorService;

@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "project", description = "User Related API")
@RequestMapping("/api/v1/project")
@RestController
public class ProjectController {

    private final ValidatorService validator;
    private final ProjectService projectService;

    @Operation(summary = "Create project")
    @PostMapping("/create")
    public BaseResponse<ProjectDto> create(
            @RequestBody @Valid ProjectCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Project project = projectService.createProject(requestDto, userDetails.getUser());
        return BaseResponse.success("Project created", ProjectDto.fromEntity(project));
    }

    @Operation(summary = "Get project by id")
    @GetMapping("/id/{id}")
    public BaseResponse<ProjectBasicDto> getProjectInfo(
            @PathVariable Long id) {
        var responseDto = this.projectService.getProjectBasicInfo(id);
        return BaseResponse.success("Project basic info", responseDto);
    }

    @Operation(summary = "Become a project's translator")
    @PutMapping("/join")
    public BaseResponse<Object> join(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid JoinProjectRequestDto requestDto) {
        this.projectService.joinProject(userDetails.getUser(), requestDto.getId());
        return BaseResponse.success("Joined successfully");
    }

    @Operation(summary = "Search projects")
    @GetMapping("/search")
    public BaseResponse<List<ProjectPublicDto>> searchProjects(
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "latest") String sortBy) {
        ProjectSearchRequestDto requestDto = new ProjectSearchRequestDto(keywords, category, page, sortBy);
        validator.validate(requestDto);

        var responseDto = this.projectService.searchProjects(requestDto);
        return BaseResponse.success("Search Projects", responseDto);
    }

    @Operation(summary = "Update project")
    @PutMapping("/update/id/{id}")
    public BaseResponse<ProjectBasicDto> updateProject(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ProjectUpdateRequestDto requestDto,
            @RequestParam Long id) {
        Project project = this.projectService.updateProject(userDetails.getUser(), id, requestDto);
        return BaseResponse.success("Project updated", ProjectBasicDto.fromEntity(project));
    }

};
