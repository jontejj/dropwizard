package io.dropwizard.jersey;

import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.jersey.dummy.DummyResource;
import io.dropwizard.logging.LoggingFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

public class DropwizardResourceConfigTest
{
	static
	{
		LoggingFactory.bootstrap();
	}

	private DropwizardResourceConfig rc;

	@Before
	public void setUp()
	{
		rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
	}

	@Test
	public void findsResourceClassInPackage()
	{
		rc.packages(DummyResource.class.getPackage().getName());

		assertThat(rc.getClasses()).contains(DummyResource.class);
	}

	@Test
	public void findsResourceClassesInPackageAndSubpackage()
	{
		rc.packages(getClass().getPackage().getName());

		assertThat(rc.getClasses()).contains(DummyResource.class, TestResource.class, ResourceInterface.class);
	}

	@Test
	public void combinesAlRegisteredClasses()
	{
		rc.register(new TestResource());
		rc.registerClasses(ResourceInterface.class, ImplementingResource.class);

		assertThat(rc.allClasses()).contains(TestResource.class, ResourceInterface.class, ImplementingResource.class);
	}

	@Test
	@Ignore
	public void logsNoInterfaces()
	{
		rc.packages(getClass().getPackage().getName());

		assertThat(rc.getEndpointsInfo()).doesNotContain("io.dropwizard.jersey.DropwizardResourceConfigTest.ResourceInterface");
	}

	@Test
	public void logsNoEndpointsWhenNoResourcesAreRegistered()
	{
		assertThat(rc.getEndpointsInfo()).contains("    NONE");
	}

	@Test
	public void logsEndpoints()
	{
		rc.register(TestResource.class);
		rc.register(ImplementingResource.class);

		assertThat(rc.getEndpointsInfo()).contains("GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
				.contains("GET     /another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
	}

	@Path("/dummy")
	public static class TestResource
	{
		@GET
		public String foo()
		{
			return "bar";
		}
	}

	@Path("/another")
	public static interface ResourceInterface
	{
		@GET
		public String bar();
	}

	public static class ImplementingResource implements ResourceInterface
	{
		@Override
		public String bar()
		{
			return "";
		}
	}
}
