var builder = WebApplication.CreateBuilder(args);

// lets docker and kubernetes ask if the service is alive, like actuator health in spring
builder.Services.AddHealthChecks();

var app = builder.Build();

app.MapGet("/", () => "Taste service is running");
app.MapHealthChecks("/health");

app.Run();