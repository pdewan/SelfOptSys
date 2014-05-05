package commonutils.basic2;
public enum ConfigParamType {
	STRING { public String toString() { return "STRING"; } },
	STRING_ARRAY { public String toString() { return "STRING ARRAY"; } },
	BOOLEAN { public String toString() { return "BOOLEAN"; } },
	BOOLEAN_ARRAY { public String toString() { return "BOOLEAN ARRAY"; } },
	INT { public String toString() { return "INT"; } },
	INT_ARRAY { public String toString() { return "INT ARRAY"; } },
	DOUBLE { public String toString() { return "DOUBLE"; } },
	DOUBLE_ARRAY { public String toString() { return "DOUBLE ARRAY"; } }
}