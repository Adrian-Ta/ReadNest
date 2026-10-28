package org.fg.readnest;

import java.util.ArrayList;

public record SearchResult(ArrayList<Buch> buchList, ArrayList<Autor> autorList, ArrayList<Verlag> verlagList) {
}
