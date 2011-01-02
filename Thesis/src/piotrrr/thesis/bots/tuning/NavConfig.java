package piotrrr.thesis.bots.tuning;

import java.io.Serializable;
import java.lang.reflect.Field;

public class NavConfig implements Serializable {

    public static final double initialStepSize = 0.4;
    private static final double initialVals = 0.4;
    public OptParam weight_health = new OptParam(0.4f, 0, 1, initialStepSize);
    public OptParam weight_armor = new OptParam(0.3f, 0, 1, initialStepSize);
    public OptParam weight_weapon = new OptParam(0.3f, 0, 1, initialStepSize);
    public OptParam weight_ammo = new OptParam(0.2f, 0, 1, initialStepSize);
    public OptParam weight_health_ben = new OptParam(0.5f, 0, 1, initialStepSize);
    public OptParam weight_armor_ben = new OptParam(0.4f, 0, 1, initialStepSize);
    public OptParam weight_weapon_ben = new OptParam(0.5f, 0, 1, initialStepSize);
    public OptParam weight_ammo_ben = new OptParam(0.2f, 0, 1, initialStepSize);
    public OptParam weight_distance = new OptParam(0.5f, 0, 1, initialStepSize);
    public OptParam weight_enemycost = new OptParam(0.5f, 0, 1, initialStepSize);
    public OptParam weight_aggresiveness = new OptParam(0.2f, 0, 1, initialStepSize);
    public String additionalInfo = "";

    public NavConfig() {
    }

    public NavConfig(NavConfig original) {
        weight_health = new OptParam(original.weight_health);
        weight_armor = new OptParam(original.weight_armor);
        weight_weapon = new OptParam(original.weight_weapon);
        weight_ammo = new OptParam(original.weight_ammo);

        weight_health_ben = new OptParam(original.weight_health_ben);
        weight_armor_ben = new OptParam(original.weight_armor_ben);
        weight_weapon_ben = new OptParam(original.weight_weapon_ben);
        weight_ammo_ben = new OptParam(original.weight_ammo_ben);

        weight_distance = new OptParam(original.weight_distance);
        weight_enemycost = new OptParam(original.weight_enemycost);
        weight_aggresiveness = new OptParam(original.weight_aggresiveness);
    }

    public void randAllParams() {
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p = (OptParam) f.get(this);
                p.setRandomValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setInitialParams() {
        try {
            weight_health.setValue(initialVals);
            weight_armor.setValue(initialVals);
            weight_weapon.setValue(initialVals);
            weight_ammo.setValue(initialVals);
            weight_health_ben.setValue(initialVals);
            weight_armor_ben.setValue(initialVals);
            weight_weapon_ben.setValue(initialVals);
            weight_ammo_ben.setValue(initialVals);
            weight_distance.setValue(initialVals);
            weight_enemycost.setValue(initialVals);
            weight_aggresiveness.setValue(initialVals);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getParamsCount() {
        int c = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            if (!f.getType().equals(OptParam.class)) {
                continue;
            }
            c++;
        }
        return c;
    }

    public String getParamsName(int ind) {
        String c = "no such field";
        int i=0;
        for (Field f : this.getClass().getDeclaredFields()) {
            if (!f.getType().equals(OptParam.class)) {
                continue;
            }
            if (ind == i) {
                c = f.getName();
            }
            i++;
        }
        return c;
    }

    public boolean incParam(int param) {
        int c = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                if (c == param) {
                    OptParam p = (OptParam) f.get(this);
                    return p.inc();
                }
                c++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean decParam(int param) {
        int c = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                if (c == param) {
                    OptParam p = (OptParam) f.get(this);
                    return p.dec();
                }
                c++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean setParam(int param, double value) {
        int c = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                if (c == param) {
                    OptParam p = (OptParam) f.get(this);
                    return p.setValue(value);
                }
                c++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public boolean addToParam(int param, double value) {
        int c = 0;
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                if (c == param) {
                    OptParam p = (OptParam) f.get(this);
                    return p.setValue(p.getValue()+value);
                }
                c++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String ret = "NavConfig: \n";
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p = (OptParam) f.get(this);
                ret += f.getName() + ": " + p.getValue() + "\n";
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ret += "Additional info: " + additionalInfo;
        return ret + "\n";
    }

    public String getDifferences(NavConfig c) {
        String ret = "NavConfig diffs: \n\n";
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p1 = (OptParam) f.get(this);
                OptParam p2 = (OptParam) f.get(c);
                if (p1.getValue() != p2.getValue()) {
                    ret += "orig." + f.getName() + "=" + p1.getValue() + "\nnew." + f.getName() + "=" + p2.getValue() + "\n\n";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ret + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NavConfig) {
            for (Field f : this.getClass().getDeclaredFields()) {
                try {
                    if (!f.getType().equals(OptParam.class)) {
                        continue;
                    }
                    OptParam p1 = (OptParam) f.get(this);
                    OptParam p2 = (OptParam) f.get(obj);

                    if (!p1.equals(p2)) {
                        return false;
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }

            }
            return true;

        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.weight_health != null ? this.weight_health.hashCode() : 0);
        return hash;
    }

    public void setStepSize(double ss) {
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p1 = (OptParam) f.get(this);
                p1.setStep(ss);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
