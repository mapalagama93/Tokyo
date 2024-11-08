package co.mahmm.tokyo;

import co.mahmm.tokyo.commons.FileReader;
import co.mahmm.tokyo.commons.Log;
import co.mahmm.tokyo.commons.YamlParser;
import co.mahmm.tokyo.commons.spec.DataSpec;
import co.mahmm.tokyo.commons.spec.RunSpec;
import co.mahmm.tokyo.commons.spec.ScenarioSpec;
import co.mahmm.tokyo.core.Scenario;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DynamicContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Getter
@Setter
public class SpecRunner {

    private String specFilePath;
    private List<String> configFiles;
    private ScenarioSpec spec;
    private RunSpec runSpec;
    private Scenario scenario;

    public SpecRunner(RunSpec runSpec) {
        this.specFilePath = runSpec.getScenarioSpec();
        this.configFiles = runSpec.getConfigFiles();
        this.runSpec = runSpec;
        this.parseFiles();
    }

    public Stream<DynamicContainer> run() {
        List<DataSpec> i = new ArrayList<>();
        if (this.spec.getInputs().size() == 0) {
            Log.debug("Scenario inputs not found. Running one round");
            i.add(new DataSpec());
        }else{
            i = this.spec.getInputs();
        }
        scenario = new Scenario();
        scenario.initialize(this.spec, i);
        return scenario.run();
    }


    private void parseFiles() {
        Log.debug("Parse scenario file = {}", this.specFilePath);
        String content = FileReader.readFile(this.specFilePath);
        this.spec = YamlParser.parse(content, ScenarioSpec.class);
        Map<String, String> configs = new HashMap<>();
        configs.putAll(this.spec.getConfigs());
        if(this.configFiles != null) {
            for (String configFile : this.configFiles) {
                Log.debug("Parse config file = {}", configFile);
                Map<String, String> env = YamlParser.parse(FileReader.readFile(configFile), Map.class);
                configs.putAll(env);
            }
        }
        Log.debug("All loaded config for scenario = {}", configs);
        this.spec.setConfigs(configs);

    }
}
