package server;

import java.util.ArrayList;

/**
 * This class stores simple class or join code information.
 */
public class CodeGenerator {

    private String prefix;

    private int nextNumber;

    private ArrayList<String> activeCodes;

    public CodeGenerator() {

        this("CLS", 1);

    }

    public CodeGenerator(String prefix, int nextNumber) {

        this.prefix = prefix;

        this.nextNumber = nextNumber;

        this.activeCodes = new ArrayList<String>();

    }

    public String getPrefix() { return prefix; }

    public void setPrefix(String prefix) { this.prefix = prefix; }

    public int getNextNumber() { return nextNumber; }

    public void setNextNumber(int nextNumber) { this.nextNumber = nextNumber; }

    public ArrayList<String> getActiveCodes() { return activeCodes; }

    public void setActiveCodes(ArrayList<String> activeCodes) { this.activeCodes = activeCodes; }

}
