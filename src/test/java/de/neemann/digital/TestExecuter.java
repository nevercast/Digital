package de.neemann.digital;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.library.ElementLibrary;
import de.neemann.digital.gui.draw.model.ModelBuilder;
import de.neemann.digital.gui.draw.model.ModelDescription;
import de.neemann.digital.gui.draw.model.ModelEntry;
import de.neemann.digital.integration.Resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author hneemann
 */
public class TestExecuter {

    private final Model model;
    private ArrayList<ObservableValue> inputs;
    private ArrayList<ObservableValue> outputs;

    public static TestExecuter createFromFile(String name, ElementLibrary library) throws IOException, NodeException, PinException {
        File filename = new File(Resources.getRoot(), name);
        Circuit circuit = Circuit.loadCircuit(filename);

        ModelBuilder mb = new ModelBuilder(circuit);
        Model model = mb.build(library);

        return new TestExecuter(model, true).setUp(mb.getModelDescription());
    }


    public TestExecuter() throws NodeException {
        this(null);
    }
    public TestExecuter(Model model) throws NodeException {
        this(model, false);
    }

    public TestExecuter(Model model, boolean noise) throws NodeException {
        this.model = model;
        if (model != null)
            model.init(noise);
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public TestExecuter setInputs(ObservableValue... values) {
        inputs.addAll(Arrays.asList(values));
        return this;
    }

    public TestExecuter setInputs(List<ModelEntry> entries) {
        for (ModelEntry me : entries)
            setInputs(me);
        return this;
    }

    private void setInputs(ModelEntry me) {
        setInputs(me.getIoState().getOutputs());
    }


    public TestExecuter setOutputs(ObservableValue... values) {
        outputs.addAll(Arrays.asList(values));
        return this;
    }

    public TestExecuter setOutputs(List<ModelEntry> entries) {
        for (ModelEntry me : entries)
            setOutputs(me);
        return this;
    }

    private void setOutputs(ModelEntry me) {
        setOutputs(me.getIoState().getInputs());
    }

    public TestExecuter setUp(ModelDescription modelDescription) {
        List<ModelEntry> inputs = modelDescription.getEntries("In");
        List<ModelEntry> outputs = modelDescription.getEntries("Out");

        for (ModelEntry input : inputs) {
            assertEquals(0, input.getIoState().inputCount());
            assertEquals(1, input.getIoState().outputCount());
        }
        for (ModelEntry output : outputs) {
            assertEquals(1, output.getIoState().inputCount());
            assertEquals(0, output.getIoState().outputCount());
        }

        setInputs(inputs);
        setOutputs(outputs);
        return this;
    }

    public TestExecuter setOutputsOf(Element element) {
        setOutputs(element.getOutputs());
        return this;
    }

    public void check(int... val) throws NodeException {
        for (int i = 0; i < inputs.size(); i++) {
            inputs.get(i).setValue(val[i]);
        }
        if (model != null)
            model.doStep();

        for (int i = 0; i < outputs.size(); i++) {
            int should = val[i + inputs.size()];
            if (should >= 0)
                assertEquals("output " + i, outputs.get(i).getValueBits(should), outputs.get(i).getValue());
        }
    }

    public Model getModel() {
        return model;
    }
}
