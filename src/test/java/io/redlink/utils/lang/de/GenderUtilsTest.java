/*
 * Copyright 2017 redlink GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.redlink.utils.lang.de;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class GenderUtilsTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[] { "Tankwart (m./w.)",
                        "Tankwart", "Tankwart"},
                new Object[] { "Landwirtschaftlicher Anbauberater, Landwirtschaftliche Anbauberaterin",
                        "Landwirtschaftlicher Anbauberater", "Landwirtschaftliche Anbauberaterin" },
                new Object[] { "ÖkologieberaterIn (Garten-, Land- und Forstwirtschaft)",
                        "Ökologieberater (Garten-, Land- und Forstwirtschaft)", "Ökologieberaterin (Garten-, Land- und Forstwirtschaft)" },
                new Object[] { "Prüfer/in (Sortierung/Verpackung von Waren)",
                        "Prüfer (Sortierung/Verpackung von Waren)", "Prüferin (Sortierung/Verpackung von Waren)" },
                new Object[] { "Dipl. KrankenpflegerIn, Dipl. Krankenschwester",
                        "Dipl. Krankenpfleger", "Dipl. Krankenschwester" },
                new Object[] { "Dipl. psychiatrischeR Gesundheits- und Krankenpfleger/-schwester",
                        "Dipl. psychiatrischer Gesundheits- und Krankenpfleger", "Dipl. psychiatrische Gesundheits- und Krankenschwester" },
                new Object[] { "Einzelhandelskaufmann/-frau",
                        "Einzelhandelskaufmann", "Einzelhandelskauffrau" },
                new Object[] { "Informationstechnolog(e)in - Technik",
                        "Informationstechnologe - Technik", "Informationstechnologin - Technik" },
                new Object[] { "Anlage-, Vermögens- und FinanzberaterIn",
                        "Anlage-, Vermögens- und Finanzberater", "Anlage-, Vermögens- und Finanzberaterin" },
                new Object[] { "Commis EntremetierE (Gehilfe/Gehilfin von Beilagenkoch/-köchin)",
                        "Commis Entremetier (Gehilfe von Beilagenkoch)", "Commis Entremetiere (Gehilfin von Beilagenköchin)" },
                new Object[] { "Süßspeisenkoch, Süßspeisenköchin (ConfiseurIn)",
                        "Süßspeisenkoch (Confiseur)", "Süßspeisenköchin (Confiseurin)" },
                new Object[] { "Chef EntremetierE (Leitender Beilagenkoch, Leitende Beilagenköchin)",
                        "Chef Entremetier (Leitender Beilagenkoch)", "Chef Entremetiere (Leitende Beilagenköchin)" },
                new Object[] { "Entwicklungskoch, Entwicklungsköchin (Fertiggerichteindustrie, Systemgastronomie)",
                        "Entwicklungskoch (Fertiggerichteindustrie, Systemgastronomie)", "Entwicklungsköchin (Fertiggerichteindustrie, Systemgastronomie)" },
                new Object[] { "Beilagenkoch, Beilagenköchin (EntremetierE)",
                        "Beilagenkoch (Entremetier)", "Beilagenköchin (Entremetiere)" },
                new Object[] { "Schausteller/innen, andere Unterhaltungsberufe",
                        "Schausteller, andere Unterhaltungsberufe", "Schaustellerinnen, andere Unterhaltungsberufe" },
                new Object[] { "Hauptschullehrer/innen",
                        "Hauptschullehrer", "Hauptschullehrerinnen" }
        );
    }

    private final String input;
    private final String male;
    private final String female;

    public GenderUtilsTest(String input, String male, String female) {
        this.input = input;
        this.male = male;
        this.female = female;
    }

    @Test
    public void testDegender() throws Exception {
        final List<String> degendered = Arrays.asList(GenderUtils.degender(input));

        Assert.assertThat(degendered, Matchers.hasSize(Matchers.greaterThanOrEqualTo(1)));
        Assert.assertThat(degendered, Matchers.hasItem(male));
        Assert.assertThat(degendered, Matchers.hasItem(female));
    }

}