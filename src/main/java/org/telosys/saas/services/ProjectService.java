package org.telosys.saas.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telosys.saas.dao.StorageDao;
import org.telosys.saas.dao.StorageDaoProvider;
import org.telosys.saas.domain.Entity;
import org.telosys.saas.domain.File;
import org.telosys.saas.domain.Folder;
import org.telosys.saas.domain.Generation;
import org.telosys.saas.domain.GenerationErrorResult;
import org.telosys.saas.domain.GenerationResult;
import org.telosys.saas.domain.Model;
import org.telosys.saas.domain.Project;
import org.telosys.saas.domain.ProjectConfiguration;
import org.telosys.saas.domain.ProjectConfigurationVariables;
import org.telosys.saas.domain.Template;
import org.telosys.saas.util.FileUtil;
import org.telosys.tools.api.TelosysModelException;
import org.telosys.tools.api.TelosysProject;
import org.telosys.tools.commons.TelosysToolsException;
import org.telosys.tools.commons.bundles.TargetDefinition;
import org.telosys.tools.commons.cfg.TelosysToolsCfg;
import org.telosys.tools.commons.variables.Variable;
import org.telosys.tools.dsl.DslModelUtil;
import org.telosys.tools.generator.GeneratorException;
import org.telosys.tools.generator.task.ErrorReport;
import org.telosys.tools.generator.task.GenerationTaskResult;
import org.telosys.tools.users.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProjectService {

    protected static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final StorageDao storageDao;

    public ProjectService() {
        this.storageDao = StorageDaoProvider.getStorageDao();
    }

    ProjectService(StorageDao storageDao) {
        this.storageDao = storageDao;
    }

    public TelosysProject getTelosysProject(User user, Project project) {
        String projectFolderAbsolutePath = storageDao.getProjectPath(user, project);
        TelosysProject telosysProject = new TelosysProject(projectFolderAbsolutePath);
        return telosysProject;
    }

    public void initProject(User user, Project project) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        telosysProject.initProject();
    }

    public List<Model> getModels(User user, Project project) {
        List<Model> models = new ArrayList<Model>();
        for (String modelName : getModelNames(user, project)) {
            Model model = getModel(user, project, modelName);
            if (model != null) {
                models.add(model);
            }
        }
        return models;
    }

    public List<String> getModelNames(User user, Project project) {
        List<String> filters = new ArrayList<>();
        Folder folder = storageDao.getFolderForProjectAndUser(user, project, "TelosysTools", filters);
        List<String> modelNames = new ArrayList<>();
        for (File file : folder.getFiles()) {
            if (file.getName().indexOf(".model") != -1) {
                modelNames.add(file.getName().substring(0, file.getName().indexOf(".model")));
            }
        }
        return modelNames;
    }

    public String getModelPath(User user, Project project, String modelName) {
        return FileUtil.join(storageDao.getProjectPath(user, project), "TelosysTools", modelName);
    }
    
    public Model getModel(User user, Project project, String modelName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
		try {
			org.telosys.tools.generic.model.Model genericModel = telosysProject.loadModel(modelName + ".model");
            Model model;
            if (genericModel == null) {
                model = new Model();
                model.setName(modelName);
            } else {
                model = map(telosysProject.loadModel(modelName + ".model"), modelName);
            }
            return model;
		} catch (TelosysToolsException ex) {
			if ( ex instanceof TelosysModelException ) {
				logger.error("Invalid model !");
				// Print parsing errors
				TelosysModelException tme = (TelosysModelException) ex ;
				Map<String,String> parsingErrors = tme.getParsingErrors();
				if ( parsingErrors != null ) {
					logger.error( parsingErrors.size() + " parsing error(s)" );
					for ( Map.Entry<String,String> entry : parsingErrors.entrySet() ) {
						logger.error( "'" + entry.getKey() + "' : " + entry.getValue() );
					}					
				}
			}
			else {
				logger.error("getModel", ex);					
			}
		}
		return null ; // Model cannot be loaded
        /*
        try {
            GenericModelLoader genericModelLoader = telosysProject.getGenericModelLoader();
            org.telosys.tools.generic.model.Model genericModel = genericModelLoader.loadModel(modelName + ".model");
            Model model;
            if (genericModel == null) {
                model = new Model();
                model.setName(modelName);
            } else {
                model = map(telosysProject.loadModel(modelName + ".model"), modelName);
            }
            if (genericModelLoader.getParsingErrors() != null && !genericModelLoader.getParsingErrors().isEmpty()) {
                Enumeration<String> keyEnumeration = genericModelLoader.getParsingErrors().keys();
                while (keyEnumeration.hasMoreElements()) {
                    String file = keyEnumeration.nextElement();
                    String message = genericModelLoader.getParsingErrors().get(file);
                    String entityName = file;
                    if (file.indexOf(".entity") != -1) {
                        entityName = file.substring(0, file.indexOf(".entity"));
                    }
                    ParsingError parsingError = new ParsingError();
                    parsingError.setEntityName(entityName);
                    parsingError.setMessage(message);
                    model.getParsingErrors().add(parsingError);
                }
            }
            return model;
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
        */
    }

    public Model createModel(User user, Project project, String modelName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        try {
            java.io.File file = telosysProject.getDslModelFile(modelName);
            if (!file.exists()) {
                file = telosysProject.createNewDslModel(modelName);
            }
            return map(telosysProject.loadModel(modelName + ".model"), modelName);
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    public void deleteModel(User user, Project project, String modelName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        try {
            java.io.File file = telosysProject.getDslModelFile(modelName);
            if (file.exists()) {
                telosysProject.deleteDslModel(modelName);
            }
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    private Model map(org.telosys.tools.generic.model.Model genericModel, String modelName) {
        Model model = new Model();
        model.setModelName(modelName);
        model.setName(genericModel.getName());
        model.setType(genericModel.getType());
        model.setVersion(genericModel.getVersion());
        model.setDescription(genericModel.getDescription());
        model.setDatabaseId(genericModel.getDatabaseId());
        model.setDatabaseProductName(genericModel.getDatabaseProductName());
        List<Entity> entities = new ArrayList<Entity>();
        for (org.telosys.tools.generic.model.Entity genericEntity : genericModel.getEntities()) {
            entities.add(map(genericEntity));
        }
        model.setEntities(entities);
        return model;
    }

    private Entity map(org.telosys.tools.generic.model.Entity genericEntity) {
        Entity entity = new Entity();
        entity.setFullName(genericEntity.getFullName());
        return entity;
    }

    public void addBundleToTheProject(User user, Project project, String githubUserName, String bundleName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        try {
            telosysProject.downloadAndInstallBundle(githubUserName, bundleName);
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeBundleFromTheProject(User user, Project project, String bundleName) {
        String folderTemplatesPath = FileUtil.join("TelosysTools", "templates", bundleName);
        List<String> filters = new ArrayList<>();
        Folder folderBundle = storageDao.getFolderForProjectAndUser(user, project, folderTemplatesPath, filters);
        storageDao.deleteFolderForProjectAndUser(user, project, folderBundle);
    }

    public ProjectConfiguration getProjectConfiguration(User user, Project project) {
        try {
            TelosysProject telosysProject = getTelosysProject(user, project);
            TelosysToolsCfg telosysToolsCfg = telosysProject.loadTelosysToolsCfg();

            ProjectConfiguration projectConfiguration = new ProjectConfiguration();
            ProjectConfigurationVariables projectVariables = projectConfiguration.getVariables();

            projectVariables.setSRC(telosysToolsCfg.getSRC());
            projectVariables.setTEST_SRC(telosysToolsCfg.getTEST_SRC());
            projectVariables.setRES(telosysToolsCfg.getRES());
            projectVariables.setTEST_RES(telosysToolsCfg.getTEST_RES());
            projectVariables.setWEB(telosysToolsCfg.getWEB());
            projectVariables.setDOC(telosysToolsCfg.getDOC());
            projectVariables.setTMP(telosysToolsCfg.getTMP());
            projectVariables.setROOT_PKG(telosysToolsCfg.getRootPackage());
            projectVariables.setENTITY_PKG(telosysToolsCfg.getEntityPackage());

            Map<String, String> specificVariables = new HashMap<String, String>();
            for (Variable variable : telosysToolsCfg.getSpecificVariables()) {
                specificVariables.put(variable.getName(), variable.getValue());
            }
            String specificVariablesAsJson = new ObjectMapper().writeValueAsString(specificVariables);
            projectConfiguration.getVariables().setSpecificVariables(specificVariablesAsJson);

            return projectConfiguration;
        } catch (TelosysToolsException | JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void saveProjectConfiguration(User user, Project project, ProjectConfiguration projectConfiguration) {
        try {
            TelosysProject telosysProject = getTelosysProject(user, project);
            TelosysToolsCfg telosysToolsCfg = telosysProject.loadTelosysToolsCfg();

            ProjectConfigurationVariables projectVariables = projectConfiguration.getVariables();

            telosysToolsCfg.setSRC(projectVariables.getSRC());
            telosysToolsCfg.setTEST_SRC(projectVariables.getTEST_SRC());
            telosysToolsCfg.setRES(projectVariables.getRES());
            telosysToolsCfg.setTEST_RES(projectVariables.getTEST_RES());
            telosysToolsCfg.setWEB(projectVariables.getWEB());
            telosysToolsCfg.setDOC(projectVariables.getDOC());
            telosysToolsCfg.setTMP(projectVariables.getTMP());
            telosysToolsCfg.setRootPackage(projectVariables.getROOT_PKG());
            telosysToolsCfg.setEntityPackage(projectVariables.getENTITY_PKG());

            JSONObject json = (JSONObject) new JSONParser().parse(projectVariables.getSpecificVariables());
            List<Variable> variables = new ArrayList<>();
            for (Object key : json.keySet()) {
                String name = (String) key;
                String value = String.valueOf(json.get(key));
                Variable variable = new Variable(name, value);
                variables.add(variable);
            }
            telosysToolsCfg.setSpecificVariables(variables);
            telosysProject.saveTelosysToolsCfg(telosysToolsCfg);

        } catch (TelosysToolsException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Template> getTemplatesForGeneration(User user, Project project, String bundleName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        List<Template> templateList = new ArrayList<>();
        try {
            List<TargetDefinition> targetDefinitions = telosysProject.getTargetDefinitions(bundleName).getTemplatesTargets();
            for (TargetDefinition targetDefinition : targetDefinitions) {
                Template template = new Template(targetDefinition);
                String absoluteFilePath = "TelosysTools/templates/" + bundleName + "/" + targetDefinition.getTemplate();
                template.setAbsoluteFilePath(absoluteFilePath);
                templateList.add(template);
            }
            return templateList;
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    public GenerationResult launchGeneration(User user, Project project, Generation generation) {
        return launchGenerationByEntityAndBundle(user, project, generation.getModel(), generation.getEntities(), generation.getBundle(), generation.getTemplates());
    }

    public GenerationResult launchGenerationByEntityAndBundle(User user, Project project, String modelName, List<String> entityNames, String bundleName, List<String> templatesName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        try {
            org.telosys.tools.generic.model.Model genericModel = telosysProject.loadModel(modelName + ".model");

            List<TargetDefinition> targetDefinitions = telosysProject.getTargetDefinitions(bundleName).getTemplatesTargets();
            List<TargetDefinition> targetDefinitions1 = new ArrayList<>();
            for (TargetDefinition targetDefinition : targetDefinitions) {
                if (templatesName.contains(targetDefinition.getTemplate())) {
                    targetDefinitions1.add(targetDefinition);
                }
            }
            logger.info("launchGenerationByEntityAndBundle : start of generation");
            GenerationTaskResult generationTaskResult = telosysProject.launchGeneration(genericModel, entityNames, bundleName, targetDefinitions1, true);
            logger.info("launchGenerationByEntityAndBundle : end of generation");
            GenerationResult generationResult = new GenerationResult();
            generationResult.setNumberOfFilesGenerated(generationTaskResult.getNumberOfFilesGenerated());
            generationResult.setNumberOfGenerationErrors(generationTaskResult.getNumberOfGenerationErrors());
            generationResult.setNumberOfResourcesCopied(generationTaskResult.getNumberOfResourcesCopied());
            for (ErrorReport errorReport : generationTaskResult.getErrors()) {
                GenerationErrorResult error = new GenerationErrorResult();
                error.setException(errorReport.getException());
                error.setErrorType(errorReport.getErrorType());
                error.setMessage(errorReport.getMessage());
                generationResult.getErrors().add(error);
            }
            return generationResult;
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    public GenerationResult launchGenerationByModelAndBundle(User user, Project project, String modelName, String bundleName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        try {
            org.telosys.tools.generic.model.Model genericModel = telosysProject.loadModel(modelName + ".model");
            GenerationTaskResult generationTaskResult = telosysProject.launchGeneration(genericModel, bundleName);

            GenerationResult generationResult = new GenerationResult();
            generationResult.setNumberOfFilesGenerated(generationTaskResult.getNumberOfFilesGenerated());
            generationResult.setNumberOfGenerationErrors(generationTaskResult.getNumberOfGenerationErrors());
            generationResult.setNumberOfResourcesCopied(generationTaskResult.getNumberOfResourcesCopied());
            for (ErrorReport errorReport : generationTaskResult.getErrors()) {
                GenerationErrorResult error = new GenerationErrorResult();
                error.setException(errorReport.getException());
                error.setErrorType(errorReport.getErrorType());
                error.setMessage(errorReport.getMessage());
                generationResult.getErrors().add(error);
            }
            return generationResult;
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    public void createEntityForModel(User user, Project project, String modelName, String entityName) {
        TelosysProject telosysProject = getTelosysProject(user, project);
        try {
            java.io.File modelIOFile = telosysProject.getDslModelFile(modelName);
            DslModelUtil.createNewEntity(modelIOFile, entityName);
        } catch (TelosysToolsException e) {
            throw new IllegalStateException(e);
        }
    }

    public void removeProjectForUser(User user, Project project) {
        storageDao.deleteProjectForUser(user, project);
    }
}
