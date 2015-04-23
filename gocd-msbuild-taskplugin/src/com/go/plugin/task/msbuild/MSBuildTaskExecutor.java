package com.go.plugin.task.msbuild;

import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.*;
import com.thoughtworks.go.plugin.api.task.Console;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

public class MSBuildTaskExecutor implements TaskExecutor {

    @Override
    public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {
        ProcessBuilder msbuild = createMSBuildCommand(taskExecutionContext, taskConfig);

        Console console = taskExecutionContext.console();
        console.printLine("-------------------------------------------------------------------------------");
        console.printLine("|                         Starting MS Build Task                              |");
        console.printLine("-------------------------------------------------------------------------------");
        console.printLine("Launching command: " + StringUtils.join(msbuild.command(), " "));

        try {
            Process process = msbuild.start();
            
            console.readErrorOf(process.getErrorStream());
            console.readOutputOf(process.getInputStream());

            int exitCode = process.waitFor();
            process.destroy();

            if (exitCode != 0) {
                return ExecutionResult.failure("Build Failure");
            }
        }
        catch(Exception e) {
            console.printLine(e.getMessage());
            return ExecutionResult.failure("Failed while running MSBuild task ", e);
        }

        return ExecutionResult.success("Build Success");
    }

    ProcessBuilder createMSBuildCommand(TaskExecutionContext taskContext, TaskConfig taskConfig) {

        List<String> command = new ArrayList<String>();

        String msBuildPath = "C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\MSBuild.exe";
        String customizeMSBuildPath = taskConfig.getValue(MSBuildTask.CUSTOMIZEMSBUILDPATH);
        if(customizeMSBuildPath != null && customizeMSBuildPath.equals("true")) {
        	msBuildPath = taskConfig.getValue(MSBuildTask.MSBUILDPATH);
        }
        command.add(msBuildPath);
        
        AddMSBuildArguments(taskConfig, command);
        AddAdditionalParameters(taskConfig, command);
        AddProjectFile(taskConfig, command);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.directory(new File(taskContext.workingDir()));
        return processBuilder;
    }

    private void AddProjectFile(TaskConfig taskConfig, List<String> command) {
        String solutionFile = taskConfig.getValue(MSBuildTask.SOLUTIONFILE);
        command.add(solutionFile);
    }

    private void AddMSBuildArguments(TaskConfig taskConfig, List<String> command) {
        String rawProperties = taskConfig.getValue(MSBuildTask.PROPERTIES);
        if(rawProperties != null && !StringUtils.isEmpty(rawProperties)) {
        	//split props by line
        	String properties[] = rawProperties.split("[\r\n]+"); 
        	for(String prop : properties){
        		//strip any whitespace from property leaving only 'propertyName'='value'
        		prop = prop.replaceAll("\\s+", ""); 
        		command.add("/property:"+prop);
        	}
        }
        
        String verbosity = taskConfig.getValue(MSBuildTask.VERBOSITY);
        if(verbosity != null && !StringUtils.isEmpty(verbosity)) {
        	command.add("/verbosity:" + verbosity);
        } else {
        	command.add("/verbosity:normal");
        }
        
        String specifyTargets = taskConfig.getValue(MSBuildTask.SPECIFYTARGETS);
        if(specifyTargets != null && specifyTargets.equals("true")) {
        	String targets = taskConfig.getValue(MSBuildTask.TARGETS);
        	if(targets != null &&  !StringUtils.isEmpty(targets)) {
        		targets = targets.replaceAll("\\s+", "");
        		command.add("/targets:"+targets);
        	}
        }
        
        String fileLogger = taskConfig.getValue(MSBuildTask.FILELOGGER);
        if (fileLogger != null && fileLogger.equals("true")) {
            command.add("/fileLogger");
        }
        
        String detailedSummary = taskConfig.getValue(MSBuildTask.DETAILEDSUMMARY);
        if (detailedSummary != null && detailedSummary.equals("true")) {
            command.add("/detailedsummary");
        }
        
        String noLogo = taskConfig.getValue(MSBuildTask.NOLOGO);
        if (noLogo != null && noLogo.equals("true")) {
            command.add("/nologo");
        }
        
        String noAutoResponse = taskConfig.getValue(MSBuildTask.NOAUTORESPONSE);
        if (noAutoResponse != null && noAutoResponse.equals("true")) {
            command.add("/noautoResponse");
        }
    }
    
    private void AddAdditionalParameters(TaskConfig taskConfig, List<String> command) {
    	String additionalParams = taskConfig.getValue(MSBuildTask.ADDITIONALPARAMETERS);
    	String splitParams[] = additionalParams.split("[\r\n]+"); 
    	for(String param : splitParams){
    		//strip any whitespace from parameter leaving only 'propertyName'='value'
    		param = param.replaceAll("\\s+", ""); 
    		command.add(param);
    	}
    }
}