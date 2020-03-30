public class Wrapper{
    @Test
    public void fixed() throws Exception {
        Attribute attribute = TestFactory.makeAttribute(TestFactory.DEFAULT_PROVIDER, "--##string##--");
        TestFactory.makeTimedValue("--##string##--", attribute, "--##string##--", 100d);
        builder.addGeographySpecification(new GeographySpecificationBuilder("--##string##--").addMatcher("--##string##--", "--##string##--")).addAttributeSpecification("--##string##--", "--##string##--");
        engine.execute(builder.build(), writer, true);
        assertThat(writer.toString(), hasJsonPath("--##string##--", equalTo("--##string##--")));
        assertThat(writer.toString(), hasJsonPath("--##string##--", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("--##string##--", equalTo(100d)));
    }
}