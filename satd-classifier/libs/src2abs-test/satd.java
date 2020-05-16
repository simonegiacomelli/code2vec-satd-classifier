public class Wrapper{
    @Test
    public void satd() throws Exception {
        builder.addGeographySpecification(new GeographySpecificationBuilder("--##string##--").addMatcher("--##string##--", "--##string##--")).addAttributeSpecification("--##string##--", "--##string##--");
        engine.execute(builder.build(), writer, true);
        assertThat(writer.toString(), hasJsonPath("--##string##--", equalTo("--##string##--")));
        assertThat(writer.toString(), hasJsonPath("--##string##--", hasSize(1)));
        assertThat(writer.toString(), hasJsonPath("--##string##--", equalTo(1465.0)));
    }
}