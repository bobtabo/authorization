using Authorization.Api.Infrastructure.Mail;

namespace Authorization.Api.Tests.Infrastructure.Mail;

public class SesMailerTests
{
    [Fact]
    public void BuildActivationHtml_EscapesClientName()
    {
        var html = SesMailer.BuildActivationHtml("<script>alert(1)</script>", "https://example.com/activate", "App");

        Assert.DoesNotContain("<script>alert(1)</script>", html);
        Assert.Contains("&lt;script&gt;alert(1)&lt;/script&gt;", html);
    }

    [Fact]
    public void BuildActivationHtml_RejectsNonHttpActivateUrl()
    {
        var html = SesMailer.BuildActivationHtml("Client", "javascript:alert(1)", "App");

        Assert.DoesNotContain("javascript:alert(1)", html);
    }

    [Fact]
    public void BuildActivationHtml_KeepsValidHttpsActivateUrl()
    {
        var html = SesMailer.BuildActivationHtml("Client", "https://example.com/clients/1/activate", "App");

        Assert.Contains("https://example.com/clients/1/activate", html);
    }
}
